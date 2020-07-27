package org.cwi.examine.internal.visualization.overview;

import javafx.collections.ListChangeListener;
import org.cwi.examine.internal.visualization.Visualization;
import org.cwi.examine.internal.graphics.draw.PositionedSnippet;
import com.vividsolutions.jts.geom.Geometry;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.cwi.examine.internal.data.HNode;
import org.cwi.examine.internal.data.HAnnotation;
import org.cwi.examine.internal.data.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cwi.examine.internal.graphics.PVector;
import org.cwi.examine.internal.layout.Layout;
import org.cwi.examine.internal.layout.Layout.RichEdge;
import org.jgrapht.graph.DefaultEdge;

import static org.cwi.examine.internal.graphics.StaticGraphics.*;
import static org.cwi.examine.internal.graphics.draw.Parameters.labelFont;

// Network overview.
public class Overview extends PositionedSnippet {

    private final Visualization visualization;

    // Protein representations.
    private final List<NodeRepresentation> nodeRepresentations;

    // Interaction representation.
    private final List<LinkRepresentation> interactionRepresentations;

    // Set representation.
    private final List<SetContour> setRepresentations;

    // Imposed bounds.
    public PVector bounds;

    // Translation to center protein and set representations.
    public PVector span;

    // Updater thread.
    private boolean updateGoAhead;
    
    private Thread updater;
    
    // Zooming and panning.
    private double zoomFactor;
    private PVector panTranslation;
    private Point2D lastMousePos;

    public Overview(final Visualization visualization) {
        this.visualization = visualization;
        this.bounds = PVector.v();
        this.nodeRepresentations = new ArrayList<>();
        this.interactionRepresentations = new ArrayList<>();
        this.setRepresentations = new ArrayList<>();
        this.span = PVector.v();
        this.updateGoAhead = true;
        
        this.zoomFactor = 1;
        this.panTranslation = PVector.v();
    }

    // Stop update for disposal.
    public void stop() {
        updateGoAhead = false;
    }
    
    @Override
    public void draw() {
        // Constrain panning and zooming.
        double minZoom = 0.75 * (span.x > 0 && span.y > 0 ?
                                 Math.min(bounds.x / span.x, bounds.y / span.y) :
                                 1);
        zoomFactor =  Math.min(1, Math.max(minZoom, zoomFactor));
        panTranslation.x = Math.min(0.5 * span.x,
                            Math.max(-0.5 * span.x, panTranslation.x));
        panTranslation.y = Math.min(0.5 * span.y,
                            Math.max(-0.5 * span.y, panTranslation.y));
        
        // Separate SOM computation and snippet update thread.
        if(updater == null) {
            updater = new Thread(new LayoutUpdater());
            updater.setPriority(Thread.MIN_PRIORITY);
            updater.start();
        }
        
        translate(topLeft);
        
        // Background rectangle for interaction.
        color(new Color(1f,1f,1f,0f));
        picking();
        fillRect(0, 0, bounds.x, bounds.y);
        noPicking();

        // Center overview.
        noTransition();
        translate(0.5 * bounds.x, 0.5 * bounds.y);
        translate(panTranslation.x, panTranslation.y);
        scale(zoomFactor, zoomFactor);
        transition();
        
        translate(-0.5 * span.x, -0.5 * span.y);

        // Small font.
        textFont(labelFont);

        // Set bodies first, then outlines on top.
        synchronized (setRepresentations) {
            snippets(setRepresentations);

            noTransition();
            for (SetContour sR : setRepresentations) {
                sR.drawOutline();
            }
            transition();
        }
        synchronized (interactionRepresentations) {
            snippets(interactionRepresentations);
        }
        synchronized (nodeRepresentations) {
            snippets(nodeRepresentations);
        }
    }

    @Override
    public PVector dimensions() {
        return bounds;
    }

    @Override
    public void mouseWheel(int rotation) {
        zoomFactor *= Math.pow(0.8, rotation);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        PVector dPos = lastMousePos == null ?
            PVector.v() :
            PVector.v(e.getX() - lastMousePos.getX(), e.getY() - lastMousePos.getY());
        
        dPos.mult(1 / zoomFactor);
        panTranslation.add(dPos);
        
        lastMousePos = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        lastMousePos = null;
    }
    

    // Network layout updater.
    private class LayoutUpdater implements Runnable {
        private Layout layout;          // Layout model.
        private boolean layoutDirty;    // Layout has to be replaced flag.
        private Network contextNetwork; // Context network that is being visualized.
        private Contours setContours;   // Set contours.

        // Constructor.
        public LayoutUpdater() {
            // Update model for network change.
            Runnable modelObs = () -> {
                synchronized(LayoutUpdater.this) {
                    // Fetch context network (single input network, for now).
                    // Now bypassed to super network for Cytoscape integration.
                    contextNetwork = visualization.model.activeNetwork.get();
                    layoutDirty = true;
                }
            };
            visualization.model.activeNetwork.addListener((obs, old, activeNetwork) -> modelObs.run());
            visualization.model.selection.activeSetList.addListener(new ListChangeListener<HAnnotation>() {
                @Override
                public void onChanged(Change<? extends HAnnotation> c) {
                    modelObs.run();
                }
            });
        }

        @Override
        public void run() {
            // Update ad infinitum.
            while (updateGoAhead) {
                try {
                    update();
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Overview.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        // Update layout.
        private boolean converged = true;
        private void update() {
            synchronized(LayoutUpdater.this) {
                if(contextNetwork != null && !contextNetwork.graph.vertexSet().isEmpty()) {
                    if (layoutDirty || layout == null) {
                        layoutDirty = false;
                        layout = new Layout(contextNetwork, visualization.model.selection, layout);
                        updateNodeRepresentations();
                        updateInteractionRepresentations();
                        updateSetRepresentations();
                        span = layout.dimensions;

                        converged = false;
                    }

                    if (!converged) {
                        converged = layout.updatePositions();

                        // Update node positions.
                        updateNodePositions();
                        updateInteractionRepresentations();

                        // Update set contours.
                        updateSetRepresentations();

                        // Update centering shift.
                        span = layout.dimensions;
                    }
                }
            }
        }

        // Update node representations.
        private void updateNodeRepresentations() {
            // Construct nodes and push to overview.
            synchronized (nodeRepresentations) {
                nodeRepresentations.clear();
                for(HNode n: layout.nodes)
                    nodeRepresentations.add(new NodeRepresentation(visualization, n));
                updateNodePositions();
            }
        }

        private void updateNodePositions() {
            for(NodeRepresentation nR: nodeRepresentations)
                nR.topLeft(layout.position(nR.element));
        }

        // Generate protein and interaction representations.
        private void updateInteractionRepresentations() {
            // Construct interactions and push to overview.
            List<LinkRepresentation> iR = new ArrayList<LinkRepresentation>();

            for (RichEdge iE : layout.richGraph.edgeSet())
                if(iE.core) {
                    // End point proteins.
                    HNode sP = layout.richGraph.getEdgeSource(iE).element;
                    HNode tP = layout.richGraph.getEdgeTarget(iE).element;

                    // Ignore self-loops.
                    if (sP == tP) {
                        continue;
                    }

                    PVector[] intCs = new PVector[3];
                    intCs[0] = layout.position(sP);
                    intCs[1] = layout.position(iE.subNode);
                    intCs[2] = layout.position(tP);

                    // Representation.
                    DefaultEdge originalEdge = layout.network.graph.getEdge(sP, tP);
                    LinkRepresentation rep
                            = new LinkRepresentation(visualization, originalEdge, sP, tP, intCs, contextNetwork);
                    iR.add(rep);
                }

            // Transfer.
            synchronized (interactionRepresentations) {
                interactionRepresentations.clear();
                interactionRepresentations.addAll(iR);
            }
        }

        // Update set representations.
        private void updateSetRepresentations() {
            setContours = new Contours(layout);

            // Create new representations.
            List<SetContour> sR = new ArrayList<>();
            for (int i = layout.sets.size() - 1; 0 <= i; i--) {
                HAnnotation pS = layout.sets.get(i);

                // Stick to same snippet.
                Geometry bodyShape = setContours.ribbonShapes.get(i);
                Geometry outlineShape = setContours.outlineShapes.get(i);

                SetContour rep = new SetContour(visualization, pS, i, bodyShape, outlineShape);
                sR.add(rep);
            }

            // Transfer.
            synchronized (setRepresentations) {
                setRepresentations.clear();
                setRepresentations.addAll(sR);
            }
        }
    }
}

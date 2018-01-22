package org.cytoscape.examine.internal.visualization.overview;

import com.vividsolutions.jts.geom.Geometry;
import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.data.Network;
import org.cytoscape.examine.internal.graphics.AnimatedGraphics;
import org.cytoscape.examine.internal.graphics.PVector;
import org.cytoscape.examine.internal.graphics.draw.PositionedSnippet;
import org.cytoscape.examine.internal.layout.Layout;
import org.cytoscape.examine.internal.layout.Layout.RichEdge;
import org.cytoscape.examine.internal.model.Model;
import org.cytoscape.examine.internal.signal.Observer;
import org.cytoscape.examine.internal.visualization.SetColors;
import org.jgrapht.graph.DefaultEdge;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cytoscape.examine.internal.graphics.draw.Constants.LABEL_FONT;

// Network overview.
public class Overview extends PositionedSnippet {

    private final Model model;
    private final SetColors setColors;

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

    private AnimatedGraphics lastAnimatedGraphics;

    public Overview(Model model, SetColors setColors) {
        this.model = model;
        this.setColors = setColors;

        this.bounds = PVector.v();
        this.nodeRepresentations = new ArrayList<NodeRepresentation>();
        this.interactionRepresentations = new ArrayList<LinkRepresentation>();
        this.setRepresentations = new ArrayList<SetContour>();
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
    public void draw(AnimatedGraphics g) {
        lastAnimatedGraphics = g;

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
        
        g.translate(topLeft);
        
        // Background rectangle for interaction.
        g.color(new Color(1f,1f,1f,0f));
        g.picking();
        g.fillRect(0, 0, bounds.x, bounds.y);
        g.noPicking();

        // Center overview.
        g.noTransition();
        g.translate(0.5 * bounds.x, 0.5 * bounds.y);
        g.translate(panTranslation.x, panTranslation.y);
        g.scale(zoomFactor, zoomFactor);
        g.transition();

        g.translate(-0.5 * span.x, -0.5 * span.y);

        // Small font.
        g.textFont(LABEL_FONT);

        // Set bodies first, then outlines on top.
        synchronized (setRepresentations) {
            g.snippets(setRepresentations);

            g.noTransition();
            for (SetContour sR : setRepresentations) {
                sR.drawOutline(g);
            }
            g.transition();
        }
        synchronized (interactionRepresentations) {
            g.snippets(interactionRepresentations);
        }
        synchronized (nodeRepresentations) {
            g.snippets(nodeRepresentations);
        }
    }

    @Override
    public PVector dimensions(AnimatedGraphics g) {
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
            Observer modelObs = new Observer() {
                public void signal() {
                    synchronized(LayoutUpdater.this) {
                        // Fetch context network (single input network, for now).
                        // Now bypassed to super network for Cytoscape integration.
                        contextNetwork = model.activeNetwork.get();
                        layoutDirty = true;
                    }
                }
            };
            model.activeNetwork.change.subscribe(modelObs);
            model.selection.change.subscribe(modelObs);
            modelObs.signal();
        }

        @Override
        public void run() {
            // Update while no terminate has been requested.
            while (updateGoAhead) {
                try {
                    update();
                    Thread.sleep(25);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Overview.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        // Update layout.
        private void update() {
            if(lastAnimatedGraphics == null) {
                return;
            }

            synchronized(LayoutUpdater.this) {
                if (layoutDirty || layout == null) {
                    layoutDirty = false;
                    layout = new Layout(lastAnimatedGraphics, contextNetwork, model.selection, layout);
                    updateNodeRepresentations();
                    updateInteractionRepresentations();
                    updateSetRepresentations();
                    span = layout.dimensions;
                }

                if (layout.nodes.length > 0) {
                    boolean converged = layout.updatePositions(lastAnimatedGraphics);

                    if(!converged) {
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
                    nodeRepresentations.add(new NodeRepresentation(model, n));
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
                            = new LinkRepresentation(model, originalEdge, sP, tP, intCs);
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
            setContours = new Contours(lastAnimatedGraphics, layout);

            // Create new representations.
            List<SetContour> sR = new ArrayList<SetContour>();
            for (int i = layout.sets.size() - 1; 0 <= i; i--) {
                HSet pS = layout.sets.get(i);

                Geometry bodyShape = setContours.ribbonShapes.get(i);
                Geometry outlineShape = setContours.outlineShapes.get(i);

                SetContour rep = new SetContour(model, setColors, pS, i, bodyShape, outlineShape);
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

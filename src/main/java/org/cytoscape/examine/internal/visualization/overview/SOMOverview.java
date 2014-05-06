package org.cytoscape.examine.internal.visualization.overview;

import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import static org.cytoscape.examine.internal.graphics.Math.*;
import static java.lang.Math.*;
import org.cytoscape.examine.internal.graphics.draw.PositionedSnippet;
import com.vividsolutions.jts.geom.Geometry;

import org.cytoscape.examine.internal.data.Comparators;
import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.data.Network;
import org.cytoscape.examine.internal.som.Coordinates;
import org.cytoscape.examine.internal.som.SelfOrganizingMap;
import org.cytoscape.examine.internal.som.Topology;
import org.cytoscape.examine.internal.som.Trainer;

import static org.cytoscape.examine.internal.Modules.*;
import static org.cytoscape.examine.internal.visualization.Parameters.*;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cytoscape.examine.internal.graphics.PVector;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 */
public class SOMOverview extends PositionedSnippet {
    
    // Protein representations.
    private final List<NodeRepresentation> proteinRepresentations;
    
    // Interaction representation.
    private final List<InteractionRepresentation> interactionRepresentations;
    
    // Set representation.
    private final List<SetContour> setRepresentations;
    
    // Imposed bounds.
    public PVector bounds;
    
    // Translation to center protein and set representations.
    public PVector span;
    
    // Tile radius.
    public static double tileRadius;
    public static double tileSide;
    
    // Updater thread.
    private boolean updateGoAhead;

    /**
     * Base constructor.
     */
    public SOMOverview() {
        this.bounds = v();
        this.proteinRepresentations = new ArrayList<NodeRepresentation>();
        this.interactionRepresentations = new ArrayList<InteractionRepresentation>();
        this.setRepresentations = new ArrayList<SetContour>();
        this.span = v();
        this.updateGoAhead = true;
        
        // Separate SOM computation and snippet update thread.
        Thread updateThread = new Thread(new SOMUpdater());
        updateThread.setPriority(Thread.MIN_PRIORITY);
        updateThread.start();
    }
    
    /**
     * Stop update for disposal.
     */
    public void stop() {
        updateGoAhead = false;
    }

    @Override
    public void draw() {
        translate(topLeft);
        
        translate(tileRadius, tileRadius);
        
        // Center overview.
        translate(max(0, 0.5f * (bounds.x - span.x)),
                  max(0, 0.5f * (bounds.y - span.y)));
        
        // Small font.
        textFont(labelFont.get());
        
        // Set bodies first, then outlines on top.
        synchronized(setRepresentations) {
            snippets(setRepresentations);
            
            noTransition();
            for(SetContour sR: setRepresentations) {
                sR.drawOutline();
            }
            transition();
        }
        synchronized(interactionRepresentations) {
            snippets(interactionRepresentations);
        }
        synchronized(proteinRepresentations) {
            snippets(proteinRepresentations);
        }
    }
        
    /**
     * Network topology (array) coordinates to overview coordinates.
     */
    public static PVector somToOverview(Coordinates co) {
        return v((2f * co.x + ((co.y + 2) % 2)) * tileSide,
                 (1.5f * co.y) * tileRadius);
    }
    
    /**
     * Hexagonal hull points as vectors, conforming tileRadius.
     */
    public static PVector[] tilePoints() {
        PVector[] tilePoints = new PVector[7];
        
        for(int i = 0; i < 7; i++) {
            tilePoints[i] = PVector.fromAngle(Math.PI * (i + 0.5) / 3.0);
            tilePoints[i].mult(tileRadius);
        }
        
        return tilePoints;
    }

    @Override
    public PVector dimensions() {
        return bounds;
    }
    
    /**
     * Update code for SOM and snippets.
     */
    private class SOMUpdater implements Runnable {
        
        // Duration (in ms) to train until update.
        public final long TRAIN_DURATION = 500;
    
        // Mapped proteins.
        private Set<HNode> proteins;
        
        // Indexed (overview) protein list.
        private List<HNode> proteinList;
        
        // Context network (which is being visualized;
        private Network contextNetwork;
        
        // Network topology.
        private Topology topology;
        
        // Learning model.
        private LearningModel learningModel;
        
        // Self-organizing map.
        private SelfOrganizingMap som;
        
        // Network trainer.
        private Trainer trainer;
        
        // Set contour generator.
        private SOMContours somContours;
        
        /**
         * Base constructor.
         */
        public SOMUpdater() {
            proteins = new HashSet<HNode>();
            proteinList = new ArrayList<HNode>();
            
            int initialSize = 10;
            topology = new Topology(initialSize, initialSize);
        }    

        @Override
        public void run() {
            // Update ad infinitum.
            while(updateGoAhead) {
                try {
                    update();
                    Thread.sleep(10);
                } catch(InterruptedException ex) {
                    Logger.getLogger(SOMOverview.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        /**
         * Update SOM.
         */
        private void update() {
            // Fetch context network (single input network, for now).
            // Now bypassed to super network for Cytoscape integration.
            contextNetwork = model.activeNetwork.get();
            //if(contextNetwork.graph.vertexSet().isEmpty()) {
            //    return;
            //}
            
            // Determine whether topology dimensions have changed.
            boolean updatedTopology = updateTopology();
            
            // Update proteins.
            boolean updatedProteins = updateProteins(contextNetwork.graph.vertexSet());
            
            // Update protein feature data.
            boolean updateFeatures = learningModel == null ||
                                     !model.selection.activeSetList.equals(learningModel.proteinSets) ||
                                     !model.selection.activeSetMap.equals(learningModel.proteinSetWeights);

            // Plain network.
            if(som == null || updatedProteins || updateFeatures || updatedTopology) {
                // Old learning model.
                LearningModel oldLearningData = learningModel;

                // New learning model.
                learningModel = new LearningModel(proteinList,
                                                  contextNetwork,
                                                  model.selection);

                // Old SOM.
                SelfOrganizingMap oldSom = som;

                // New SOM.
                som = new SelfOrganizingMap(learningModel.size,
                                            topology,
                                            som == null);

                Trainer oldTrainer = trainer;
                trainer = new Trainer(som, learningModel);

                // Transfer old SOM configuration as much as possible.
                if(oldTrainer != null) {                    
                    // Transfer new proteins.
                    for(HNode protein: proteinList) {
                        double[] oldFeatures = oldLearningData.vectorMap.get(protein);
                        double[] newFeatures = learningModel.vectorMap.get(protein);

                        if(oldFeatures != null && newFeatures != null) {
                            // Old topology.
                            int oldClosestNeuron = oldTrainer.closestNeuron(oldFeatures);
                            int oldX = oldSom.topology.x[oldClosestNeuron];
                            int oldY = oldSom.topology.y[oldClosestNeuron];

                            // Neuron in new topology.
                            int newX = (int) round(((double) ((som.topology.xSize - 1) * oldX) /
                                                    (double) (oldSom.topology.xSize - 1)));
                            int newY = (int) round(((double) ((som.topology.ySize - 1) * oldY) /
                                                    (double) (oldSom.topology.ySize - 1)));
                            int newNeuron = som.topology.neuronAt(newX, newY);

                            trainer.set(newNeuron, newFeatures);
                        }
                    }
                }

                // Update SOM contours.
                somContours = new SOMContours(trainer);
            }
            
            if(!contextNetwork.graph.vertexSet().isEmpty()) {
                trainer.trainDancingChairsFull();
            }
            
            // Update set contours.
            updateSetRepresentations();
            
            if(updatedProteins) {
                updateProteinRepresentations();
            }
            
            // Create interaction representations.
            updateInteractionRepresentations();
            
            // Update protein positions.
            updateProteinPositions();
            
            // Update centering shift.
            PVector newSpan = somToOverview(new Coordinates(som.topology.xSize,
                                                            som.topology.ySize));
            span = newSpan;
        }
        
        /**
         * Update proteins.
         */
        private boolean updateProteins(Set<HNode> newProteins) {
            boolean update = !proteins.equals(newProteins);
            
            // Update is required.
            if(update) {
                proteins.clear();
                proteins.addAll(newProteins);

                // Arrange proteins by name and normalize in 1D space.
                // (Also functions as protein index.)
                proteinList.clear();
                proteinList.addAll(proteins);
                Collections.sort(proteinList, Comparators.stringIgnoreCase);
            }
            
            return update;
        }
        
        /**
         * Update protein representations.
         */
        private void updateProteinRepresentations() {
            // Construct proteins and push to overview.
            synchronized(proteinRepresentations) {
                proteinRepresentations.clear();
                for(int i = 0; i < learningModel.proteins.size(); i++) {
                    HNode p = learningModel.proteins.get(i);

                    double[] vector = learningModel.features[i];
                    NodeRepresentation pR =
                            new NodeRepresentation(p, vector);

                    proteinRepresentations.add(pR);
                }
                
                updateProteinPositions();
            }
        }
        
        private void updateProteinPositions() {
            for(int i = 0; i < proteinRepresentations.size(); i++) {
                NodeRepresentation pR = proteinRepresentations.get(i);
                pR.topLeft(somToOverview(trainer.proteinCoordinates[i]));
            }
        }
        
        /**
         * Generate protein and interaction representations.
         */
        private void updateInteractionRepresentations() {            
            // Construct interactions and push to overview.
            List<InteractionRepresentation> iR = new ArrayList<InteractionRepresentation>();
            
            for(DefaultEdge iE: contextNetwork.graph.edgeSet()) {
                // End point proteins.
                HNode sP = contextNetwork.graph.getEdgeSource(iE);
                HNode tP = contextNetwork.graph.getEdgeTarget(iE);

                // Make source vertex the highest degree.
                if(contextNetwork.graph.degreeOf(sP) < contextNetwork.graph.degreeOf(tP)) {
                    HNode t = sP;
                    sP = tP;
                    tP = t;
                }

                // Ignore self-loops.
                if(sP == tP) {
                    continue;
                }

                PVector[] intCs = new PVector[3];
                intCs[0] = somToOverview(trainer.coordinatesMap.get(sP));
                intCs[2] = somToOverview(trainer.coordinatesMap.get(tP));

                intCs[1] = PVector.add(intCs[0], intCs[2]);
                intCs[1].mult(0.5);

                // Add slight bend to arc.
                PVector dBE = PVector.sub(intCs[2], intCs[0]);
                dBE.mult(0.1);
                dBE = dBE.rightOrthogonal();

                intCs[1].add(dBE);

                // Ellipse space around end points of arc.
                double bufferExtent = (3 * NODE_RADIUS + tileRadius) / 4; //min(40, 0.66 * tileRadius);

                dBE = PVector.sub(intCs[1], intCs[0]);
                dBE.normalize();
                dBE.mult(bufferExtent);
                intCs[0] = PVector.add(intCs[0], dBE);

                dBE = PVector.sub(intCs[1], intCs[2]);
                dBE.normalize();
                dBE.mult(bufferExtent);
                intCs[2] = PVector.add(intCs[2], dBE);
                
                // Re-estimate bend.
                intCs[1] = PVector.add(intCs[0], intCs[2]);
                intCs[1].mult(0.5);
                
                dBE = PVector.sub(intCs[2], intCs[0]);
                dBE.mult(0.1);
                dBE = dBE.rightOrthogonal();

                intCs[1].add(dBE);

                // Representation.
                InteractionRepresentation rep =
                        new InteractionRepresentation(iE, intCs);
                iR.add(rep);
            }
            
            // Transfer.
            synchronized(interactionRepresentations) {
                interactionRepresentations.clear();
                interactionRepresentations.addAll(iR);
            }
        }
        
        /**
         * Update set representations.
         */
        private void updateSetRepresentations() {
            somContours.update();
            
            // Create new representations.
            List<SetContour> sR = new ArrayList<SetContour>();
            for(int i = 0; i < learningModel.proteinSets.size(); i++) {
                HSet pS = learningModel.proteinSets.get(i);
                
                // Stick to same snippet.
                SetContour prevRep = setRepresentations.size() > i ?
                                                setRepresentations.get(i) :
                                                null;
                
                if(prevRep != null &&
                   prevRep.outline.compareTo(somContours.setOutlineShapes.get(i)) == 0) {
                    sR.add(setRepresentations.get(i));
                } else {
                    Geometry bodyShape = somContours.setBodyShapes.get(i);
                    Geometry outlineShape = somContours.setOutlineShapes.get(i);

                    SetContour rep =
                            new SetContour(pS, i,
                                                  bodyShape, outlineShape);
                    sR.add(rep);
                }
            }            
            
            // Transfer.
            synchronized(setRepresentations) {
                setRepresentations.clear();
                setRepresentations.addAll(sR);
            }
        }
        
        /**
         * Update topology.
         */
        private boolean updateTopology() {
            boolean update = false;
            
            if(contextNetwork != null) {
                double desiredTiles = (double) contextNetwork.graph.vertexSet().size() *
                                     somTileRatio.get();
                
                int xNum = (int) (1f * sqrt(desiredTiles)) + 1;
                int yNum = (int) (1f * sqrt(desiredTiles)) + 1;
                
                // Update tile radius and derived values.
                tileRadius = min(bounds.x / ((double) sqrt(0.75) * 2 * (double) xNum),
                                 bounds.y / (1.5 * (double) yNum));
                tileRadius = min(tileRadius, somMaxTileRadius.get());
                tileSide = (double) sqrt(0.75) * tileRadius;

                update = xNum != topology.xSize ||
                         yNum != topology.ySize;

                if(update) {
                    topology = new Topology(xNum, yNum);
                }
            }
            
            return update;
        }
        
    }
    
}

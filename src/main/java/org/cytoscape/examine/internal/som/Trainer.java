package org.cytoscape.examine.internal.som;

import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.visualization.Parameters;
import org.cytoscape.examine.internal.visualization.overview.LearningModel;

import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.cytoscape.examine.internal.graphics.Math.dotProduct;

/**
 * SOM network trainer.
 */
public class Trainer {
    
    // Network to train.
    public final SelfOrganizingMap som;
    
    // Feature vectors to sample from.
    public final LearningModel learningModel;
    
    // Training iteration.
    private int iteration;
    public final int ORDERING_ITERATIONS;
    public final int CONVERGENCE_ITERATIONS;
    
    // Iteration learning rates.
    public final double INITIAL_LEARNING_RATE = 0.01f;
    public final double ORDERING_END_LEARNING_RATE = 0f;
    public final double CONVERGENCE_LEARNING_RATE = 0f;
    private double learningRate; 
    
    public final double INITIAL_NEIGHBORHOOD = 1f;
    public final double ORDERING_END_NEIGHBORHOOD = 0f;
    public final double CONVERGENCE_NEIGHBORHOOD = 0f;
    private double neighborhoodRadius;
    
    // Protein position map, by protein learning model index.
    public final int[] proteinNeurons;
    public final Coordinates[] proteinCoordinates;
    public final Map<HNode, Coordinates> coordinatesMap;
    public final int[] neuronProteins;
    
    /**
     * Base constructor.
     */
    public Trainer(SelfOrganizingMap network, LearningModel learningModel) {
        this.som = network;
        this.learningModel = learningModel;
        this.proteinNeurons = new int[learningModel.proteins.size()];
        this.proteinCoordinates = new Coordinates[learningModel.proteins.size()];
        this.coordinatesMap = new HashMap<HNode, Coordinates>();
        this.neuronProteins = new int[network.neurons.length];
        
        Arrays.fill(proteinNeurons, -1);
        
        // Normalize training iterations to number of samples.
        this.ORDERING_ITERATIONS = 1000000 / (learningModel.proteins.size() + 1);
        this.CONVERGENCE_ITERATIONS = 0; //100000 / (learningModel.proteins.size() + 1);
        
        // Training iteration count.
        this.iteration = 1;
    }
    
    /**
     * Get the neuron that is closest to the given vector.
     */
    public int closestNeuron(double[] vector) {
        int closest = -1;
        
        double minDistance = Double.POSITIVE_INFINITY;
        for(int i = 0; i < som.neurons.length; i++) {
            double[] neuronVector = som.neurons[i];
            double distance = distance(neuronVector, vector);
            
            if(distance < minDistance) {
                minDistance = distance;
                closest = i;
            }
        }
        
        return closest;
    }
    
    /**
     * Align the neurons in the neighborhood centered at the given neuron.
     */
    public void align(int neuron, double[] trainVector) {
        // Neuron array coordinates.
        int x = som.topology.x[neuron];
        int y = som.topology.y[neuron];
        
        // Neuron hexagonal coordinates.
        int hX = Topology.xArrayToHex(x, y);
        int hY = Topology.yArrayToHex(x, y);
        
        // Extent of neighborhood update.
        double thetaMin = 1 + Parameters.somNeighborhoodMin.get();
        int theta = (int) (thetaMin + neighborhoodRadius * Parameters.somNeighborhoodMax.get() *
                          (som.topology.minimumDiameter - thetaMin));
        theta = min(theta, som.topology.maximumDiameter);
        
        // For neigborhood distances.
        for(int r = 0; r < theta; r++) {
            int[] xOffsets = som.topology.xNeighborhoodOffsets[r];
            int[] yOffsets = som.topology.yNeighborhoodOffsets[r];
            
            // For neurons at specific distance.
            for(int i = 0; i < xOffsets.length; i++) {
                // Neighbor position in hexagonal space.
                int tXH = hX + xOffsets[i];
                int tYH = hY + yOffsets[i];
                
                // Neighbor position in array space.
                int tX = Topology.xHexToArray(tXH, tYH);
                int tY = Topology.yHexToArray(tXH, tYH);
                
                // Guard topology boundaries.
                if(0 <= tX && tX < som.topology.xSize &&
                   0 <= tY && tY < som.topology.ySize) {
                    // Align neuron.
                    double[] neuronVector =
                            som.neurons[som.topology.neuronAt(new Coordinates(tX, tY))];
                    for(int j = 0; j < neuronVector.length; j++) {
                        neuronVector[j] += learningRate * (trainVector[j] - neuronVector[j]);
                    }
                }
            }
        }
    }
    
    /**
     * Set the vector of a neuron.
     */
    public void set(int neuron, double[] vector) {
        double[] neuronVector = som.neurons[neuron];
        System.arraycopy(vector, 0, neuronVector, 0, neuronVector.length);
    }
    
    /**
     * Train by taken protein position.
     */
    public void trainDancingChairs() {
        // Initialize learning rates for new iteration.
        double orderingProgress = Math.min(1f, (double) iteration /
                                              (double) ORDERING_ITERATIONS);
        learningRate = iteration < ORDERING_ITERATIONS ?
                            ORDERING_END_LEARNING_RATE +
                            (INITIAL_LEARNING_RATE - ORDERING_END_LEARNING_RATE) * (1f - orderingProgress) :
                            CONVERGENCE_LEARNING_RATE;
                        
        neighborhoodRadius = iteration < ORDERING_ITERATIONS ?
                                    ORDERING_END_NEIGHBORHOOD +
                                    (INITIAL_NEIGHBORHOOD - ORDERING_END_NEIGHBORHOOD) * (1f - orderingProgress) :
                                    CONVERGENCE_NEIGHBORHOOD;
        
        // Chairs.
        boolean[] neuronTaken = new boolean[som.neurons.length];
        
        // Random index permutation for a fair game over multiple iterations.
        List<Integer> indexShuffle = new ArrayList<Integer>();
        for(int i = 0; i < learningModel.features.length; i++) {
            indexShuffle.add(i);
        }
        Collections.shuffle(indexShuffle);
        
        // Per protein (feature vector).
        for(int si = 0; si < indexShuffle.size(); si++) {
            int i = indexShuffle.get(si);
            double[] trainVector = learningModel.features[i];
            
            // Find best neuron candidate that has not been taken.
            int neuron = -1;

            double minDistance = Double.POSITIVE_INFINITY;
            for(int j = 0; j < som.neurons.length; j++) {
                double[] neuronVector = som.neurons[j];
                double distance = distance(neuronVector, trainVector);

                if(!neuronTaken[j] && distance < minDistance) {
                    minDistance = distance;
                    neuron = j;
                }
            }
            
            // Take and map neuron.
            neuronTaken[neuron] = true;
            proteinNeurons[i] = neuron;
            Coordinates co = som.topology.coordinatesOf(neuron);
            proteinCoordinates[i] = co;
            coordinatesMap.put(learningModel.proteins.get(i), co);
            
            // Train neuron.
            align(neuron, trainVector);
            
            // Increase iteration count.
            iteration++;
        }
        
        // Update neuron to protein map.
        Arrays.fill(neuronProteins, -1);
        for(int i = 0; i < proteinNeurons.length; i++) {
            neuronProteins[proteinNeurons[i]] = i;
        }
    }

    // Train until iterations have been exhausted.
    public void trainDancingChairsFull() {
        while(iteration < ORDERING_ITERATIONS + CONVERGENCE_ITERATIONS) {
            trainDancingChairs();
        }
    }
    
    // Cosine similarity distance.
    public double distance(double[] v1, double[] v2) {
        double combMag = (double) sqrt(dotProduct(v1, v1) * dotProduct(v2, v2));
        double similarity = combMag > 0 ? dotProduct(v1, v2) / combMag : 0;
        
        return 2.0 * Math.acos(similarity) / PI;
    }
    
}

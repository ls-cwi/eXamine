package org.cytoscape.examine.internal.som;

import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.som.metrics.DistanceMeasure;
import org.cytoscape.examine.internal.visualization.Parameters;
import org.cytoscape.examine.internal.visualization.overview.LearningModel;

import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * SOM network trainer.
 */
public class Trainer {
    
    // Network to train.
    public final SelfOrganizingMap som;
    
    // Feature vectors to sample from.
    public final LearningModel learningModel;
    
    // Neuron feature vector distance metric.
    public final DistanceMeasure distanceMetric;
    
    // Random number generator.
    private Random random;
    
    // Training iteration.
    private int iteration;
    public final int ORDERING_ITERATIONS;
    public final int CONVERGENCE_ITERATIONS;
    
    // Iteration learning rates.
    public final float INITIAL_LEARNING_RATE = 0.01f;
    public final float ORDERING_END_LEARNING_RATE = 0f;
    public final float CONVERGENCE_LEARNING_RATE = 0f;
    private float learningRate; 
    
    public final float INITIAL_NEIGHBORHOOD = 1f;
    public final float ORDERING_END_NEIGHBORHOOD = 0f;
    public final float CONVERGENCE_NEIGHBORHOOD = 0f;
    private float neighborhoodRadius;
    
    // Protein position map, by protein learning model index.
    public final int[] proteinNeurons;
    public final Coordinates[] proteinCoordinates;
    public final Map<HNode, Coordinates> coordinatesMap;
    public final int[] neuronProteins;
    
    /**
     * Base constructor.
     */
    public Trainer(SelfOrganizingMap network,
                   LearningModel learningModel,
                   DistanceMeasure distanceMetric) {
        this.som = network;
        this.learningModel = learningModel;
        this.distanceMetric = distanceMetric;
        this.random = new Random();
        this.proteinNeurons = new int[learningModel.proteins.size()];
        this.proteinCoordinates = new Coordinates[learningModel.proteins.size()];
        this.coordinatesMap = new HashMap<HNode, Coordinates>();
        this.neuronProteins = new int[network.neurons.length];
        
        Arrays.fill(proteinNeurons, -1);
        
        // Normalize training iterations to number of samples.
        this.ORDERING_ITERATIONS = 500000 / (learningModel.proteins.size() + 1);
        this.CONVERGENCE_ITERATIONS = 0; //100000 / (learningModel.proteins.size() + 1);
        
        // Training iteration count.
        this.iteration = 1;
    }
    
    /**
     * Get the neuron that is closest to the given vector.
     */
    public int closestNeuron(float[] vector) {
        int closest = -1;
        
        float minDistance = Float.POSITIVE_INFINITY;
        for(int i = 0; i < som.neurons.length; i++) {
            float[] neuronVector = som.neurons[i];
            float distance = distanceMetric.distance(neuronVector, vector);
            
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
    public void align(int neuron, float[] trainVector) {
        // Neuron array coordinates.
        int x = som.topology.x[neuron];
        int y = som.topology.y[neuron];
        
        // Neuron hexagonal coordinates.
        int hX = Topology.xArrayToHex(x, y);
        int hY = Topology.yArrayToHex(x, y);
        
        // Extent of neighborhood update.
        float thetaMin = 1 + Parameters.somNeighborhoodMin.get();
        int theta = (int) (thetaMin + neighborhoodRadius * Parameters.somNeighborhoodMax.get() *
                          (som.topology.minimumDiameter - thetaMin));
        theta = min(theta, som.topology.maximumDiameter);
        
        // For neigborhood distances.
        for(int r = 0; r < theta; r++) {
            int[] xOffsets = som.topology.xNeighborhoodOffsets[r];
            int[] yOffsets = som.topology.yNeighborhoodOffsets[r];
            
            // Neighborhood distribution (Gaussian).
            float neighborhoodFactor = //1f - ((float) r / (float) theta);
                                       //(float) exp((double) -(r * r) /
                                       //            (double) (2.0 * theta * theta));
                                       1f;
            
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
                    float[] neuronVector =
                            som.neurons[som.topology.neuronAt(new Coordinates(tX, tY))];
                    for(int j = 0; j < neuronVector.length; j++) {
                        neuronVector[j] += learningRate *
                                           neighborhoodFactor *
                                           (trainVector[j] - neuronVector[j]);
                    }
                }
            }
        }
    }
    
    /**
     * Set the vector of a neuron.
     */
    public void set(int neuron, float[] vector) {
        float[] neuronVector = som.neurons[neuron];
        System.arraycopy(vector, 0, neuronVector, 0, neuronVector.length);
    }
    
    /**
     * Train by taken protein position.
     */
    public void trainDancingChairs() {
        // Initialize learning rates for new iteration.
        float orderingProgress = Math.min(1f, (float) iteration /
                                              (float) ORDERING_ITERATIONS);
        learningRate = iteration < ORDERING_ITERATIONS ?
                            ORDERING_END_LEARNING_RATE +
                            (INITIAL_LEARNING_RATE - ORDERING_END_LEARNING_RATE) * (1f - orderingProgress) :
                            CONVERGENCE_LEARNING_RATE;
                        
        neighborhoodRadius = iteration < ORDERING_ITERATIONS ?
                                    ORDERING_END_NEIGHBORHOOD +
                                    (INITIAL_NEIGHBORHOOD - ORDERING_END_NEIGHBORHOOD) * (1f - orderingProgress) :
                                    CONVERGENCE_NEIGHBORHOOD;
        
        // Add noise.
        //som.addNoise(0.0001f);
        
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
            float[] trainVector = learningModel.features[i];
            
            // Find best neuron candidate that has not been taken.
            int neuron = -1;

            float minDistance = Float.POSITIVE_INFINITY;
            for(int j = 0; j < som.neurons.length; j++) {
                float[] neuronVector = som.neurons[j];
                float distance = distanceMetric.distance(neuronVector, trainVector);

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
        
        // Flatten non-taken neuron neighborhoods.
        /*float[] nullVector = new float[som.neurons[0].length];
        for(int i = 0; i < neuronTaken.length; i++) {
            if(!neuronTaken[i]) {
                align(i, nullVector);
            }
        }*/
        
        // Update neuron to protein map.
        Arrays.fill(neuronProteins, -1);
        for(int i = 0; i < proteinNeurons.length; i++) {
            neuronProteins[proteinNeurons[i]] = i;
        }
    }

    /**
     * Train until iterations have been exhausted.
     */
    public void trainDancingChairsFull() {
        while(iteration < ORDERING_ITERATIONS + CONVERGENCE_ITERATIONS) {
            trainDancingChairs();
        }
    }
    
}

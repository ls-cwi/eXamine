package org.cytoscape.examine.internal.som;

import java.util.Random;

/**
 * Self Organizing Map Network.
 */
public class SelfOrganizingMap {
    
    // Neurons weight vectors, by neuron index.
    public final float[][] neurons;
    
    // Topology.
    public final Topology topology;
    
    /**
     * Base constructor, creates random neuron vectors.
     */
    public SelfOrganizingMap(int vectorSize,
                             Topology topology,
                             boolean randomized) {        
        this.topology = topology;
        
        int numberOfNeurons = topology.size;
        neurons = new float[numberOfNeurons][vectorSize];
        
        // Random weight vector initialization.
        Random rand = new Random();
        for(int i = 0; i < neurons.length; i++) {
            for(int j = 0; j < neurons[i].length; j++) {
                neurons[i][j] = randomized ? rand.nextFloat() : 0f;
            }
        }
    }
    
    /**
     * Constructor for predefined neurons.
     */
    public SelfOrganizingMap(float[][] neurons, Topology topology) {
        this.neurons = neurons;
        this.topology = topology;
    }
    
}

package org.cytoscape.examine.internal.som.metrics;

import static aether.Math.*;

/**
 * Euclidian distance metric.
 */
public class EuclidesMetric implements DistanceMeasure {
    
    @Override
    public float distance(float[] v1, float[] v2) {
        float distance = 0;
        
        for(int i = 0; i < v1.length; i++) {
            float difference =  v1[i] - v2[i];
            distance += difference * difference;
        }
        
        return sqrt(distance);
    }

    @Override
    public String toString() {
        return "Euclides";
    }
    
}

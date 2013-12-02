package org.cytoscape.examine.internal.som.metrics;

/**
 * Manhattan distance between two float vectors.
 */
public class ManhattanMetric implements DistanceMeasure {
    
    @Override
    public float distance(float[] v1, float[] v2) {
        float distance = 0;
        
        for(int i = 0; i < v1.length; i++) {
            distance += Math.abs(v1[i] - v2[i]);
        }
        
        return distance;
    }

    @Override
    public String toString() {
        return "Manhattan";
    }
    
}

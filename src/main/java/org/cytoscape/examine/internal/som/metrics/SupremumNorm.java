package org.cytoscape.examine.internal.som.metrics;

import static aether.Math.*;

/**
 *
 */
public class SupremumNorm implements DistanceMeasure {

    @Override
    public float distance(float[] firstVector, float[] secondVector) {
        float distance = Float.NEGATIVE_INFINITY;
        
        for(int i = 0; i < firstVector.length; i++) {
            distance = max(distance, abs(firstVector[i] - secondVector[i]));
        }
        
        return distance;
    }

    @Override
    public String toString() {
        return "Supremum";
    }
    
}

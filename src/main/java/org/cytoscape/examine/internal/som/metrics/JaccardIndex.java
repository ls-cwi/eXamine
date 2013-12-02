package org.cytoscape.examine.internal.som.metrics;

import static aether.Math.*;

/**
 *
 */
public class JaccardIndex implements DistanceMeasure {

    @Override
    public float distance(float[] v1, float[] v2) {
        float intersectionSum = 0;
        float unionSum = 0;
        
        for(int i = 0; i < v1.length; i++) {
            intersectionSum += min(v1[i], v2[i]);
            unionSum += max(v1[i], v2[i]);
        }
        
        return unionSum == 0 ?
                0f :
                1f - (intersectionSum / unionSum);
    }

    @Override
    public String toString() {
        return "Fuzzy Jaccard";
    }
    
}

package org.cytoscape.examine.internal.som.metrics;

import static aether.Math.*;

/**
 *
 */
public class SorensenMeasure implements DistanceMeasure {

    //@Override
    public float abcdDistance(float a, float b, float c, float d) {
        return a == 0 ? 1f : 1f - ((2f * a) / (a + b + c));
    }

    @Override
    public String toString() {
        return "Sorensen-Dice";
    }

    @Override
    public float distance(float[] v1, float[] v2) {
        float firstProduct = dotProduct(v1, v1);
        float secondProduct = dotProduct(v2, v2);
        float combinedProduct = dotProduct(v1, v2);
        
        return firstProduct == 0 && secondProduct == 0 ?
                0f :
                1f - (2 * combinedProduct /
                     (firstProduct + secondProduct));
    }
    
}

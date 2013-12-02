package org.cytoscape.examine.internal.som.metrics;

import static aether.Math.*;

/**
 *
 */
public class TanimotoDistance implements DistanceMeasure {

    @Override
    public float distance(float[] v1, float[] v2) {
        float firstProduct = dotProduct(v1, v1);
        float secondProduct = dotProduct(v2, v2);
        float combinedProduct = dotProduct(v1, v2);
        
        return firstProduct == 0 && secondProduct == 0 ?
                1f :
                1f - (combinedProduct /
                     (firstProduct + secondProduct - combinedProduct));
    }

    @Override
    public String toString() {
        return "Tanimoto";
    }
    
}

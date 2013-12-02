package org.cytoscape.examine.internal.som.metrics;

import static aether.Math.*;

/**
 *
 */
public class CosineSimilarity implements DistanceMeasure {

    @Override
    public float distance(float[] v1, float[] v2) {
        float combMag = sqrt(dotProduct(v1, v1) * dotProduct(v2, v2));
        float similarity = combMag > 0 ? dotProduct(v1, v2) / combMag : 0f;
        
        return (float) (2.0 * Math.acos(similarity) / PI);
    }

    @Override
    public String toString() {
        return "Cosine";
    }
    
}

package org.cytoscape.examine.internal.som.metrics;

/**
 *
 */
public abstract class BinaryMetric implements DistanceMeasure {

    /**
     * Derive generic a, b, c, and d measures, which are
     * passed to a concrete metric.
     * a -> (1,1) match;
     * b -> (0,1) mismatch;
     * c -> (1,0) mismatch;
     * d -> (0,0) match.
     */
    @Override
    public float distance(float[] v1, float[] v2) {
        float a = 0;
        float b = 0;
        float c = 0;
        float d = 0;
        
        for(int i = 0; i < v1.length; i++) {
            a += v1[i] * v2[i];
            b += (1f - v1[i]) * v2[i];
            c += v1[i] * (1f - v2[i]);
            d += (1f - v1[i]) * (1f - v2[i]);
        }
        
        return abcdDistance(a, b, c, d);
    }
    
    /**
     * Implement distance measure 
     */
    public abstract float abcdDistance(float a, float b, float c, float d);
    
}

package org.cytoscape.examine.internal.som.metrics;

/**
 * Defines the distance between feature vectors.
 */
public interface DistanceMeasure {
    
    /**
     * The distance.
     */
    public float distance(float[] firstVector, float[] secondVector);
    
}

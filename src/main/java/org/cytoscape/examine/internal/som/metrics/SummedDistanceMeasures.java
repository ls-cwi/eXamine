package org.cytoscape.examine.internal.som.metrics;

/**
 * Combines two distance measures for a feature vector.
 */
public class SummedDistanceMeasures implements DistanceMeasure {
    
    // Sub distance measures.
    public final DistanceMeasure dm1, dm2;

    /**
     * Base constructor.
     */
    public SummedDistanceMeasures(DistanceMeasure dm1,
                                  DistanceMeasure dm2) {
        this.dm1 = dm1;
        this.dm2 = dm2;
    }
    
    @Override
    public float distance(float[] v1, float[] v2) {
        return dm1.distance(v1, v2) + dm2.distance(v1, v2);
    }
    
}

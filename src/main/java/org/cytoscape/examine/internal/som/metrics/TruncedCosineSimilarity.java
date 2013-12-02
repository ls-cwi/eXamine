package org.cytoscape.examine.internal.som.metrics;

import static aether.Math.*;

/**
 *
 */
public class TruncedCosineSimilarity implements DistanceMeasure {
    
    // Range start and end.
    public final int begin, end;
    
    /**
     * Base constructor.
     */
    public TruncedCosineSimilarity(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }    

    @Override
    public float distance(float[] v1, float[] v2) {
        float dotMagV1 = 0;
        float dotMagV2 = 0;
        float dotBoth = 0;
        
        for(int i = begin; i < end; i++) {
            dotMagV1 += v1[i] * v1[i];
            dotMagV2 += v2[i] * v2[i];
            dotBoth += v1[i] * v2[i];
        }
        
        float combMag = sqrt(dotMagV1 * dotMagV2);
        float similarity = combMag > 0 ? dotBoth / combMag : 0f;
        
        return (float) (2.0 * Math.acos(similarity) / PI);
    }

    @Override
    public String toString() {
        return "Cosine";
    }
    
}

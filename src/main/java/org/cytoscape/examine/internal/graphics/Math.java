package org.cytoscape.examine.internal.graphics;



/**
 * Math utility class, delegates Java and Processing functionality.
 */
public class Math {
    
    // --- Begin double array functions. ---
    
    /**
     * Linear interpolation of doubles from source to target.
     */
    public static double[] lerp(double[] source, double[] target, double amt) {
        double[] result = new double[source.length];
        
        double nAmt = 1f - amt;
        for(int i = 0; i < result.length; i++) {
            result[i] = nAmt * source[i] + amt * target[i];
        }
        
        return result;
    }
    
    /**
     * Dot product of the given arrays (or vectors).
     */
    public static double dotProduct(double[] v1, double[] v2) {
        double dotProduct = 0;
        
        for(int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
        }
        
        return dotProduct;
    }
    
    /**
     * Dot product, but limited to a range.
     */
    public static double dotProduct(double[] v1, double[] v2, int begin, int end) {
        double dotProduct = 0;
        
        for(int i = begin; i < end; i++) {
            dotProduct += v1[i] * v2[i];
        }
        
        return dotProduct;
    }
    
    // --- End double array functions. ---
    
    // --- Begin PVector functions. ---
    
    /**
     * Create 0D PVector.
     */
    public static PVector v() {
        return new PVector();
    }
    
    /**
     * Create 2D PVector.
     */
    public static PVector v(double x, double y) {
        return new PVector(x, y);
    }
    
    // --- End PVector functions. ---

}

package org.cytoscape.examine.internal.visualization;

import aether.color.Color;
import processing.core.PVector;
import static aether.color.Color.*;

/**
 * Maps node scores to colors in the form of HSB vectors.
 */
public class ScoreColorMap {
    
    /**
     * Sequence of colors that is used:
     * - (-inf, -5] to dark purple;
     * - (-5, -2.5] to purple;
     * - (-2.5, 0) to bright purple;
     * - 0 to almost white;
     * - (0, 2.5) to bright green;
     * - [2.5, 5) to green;
     * - [5, inf) to dark green.
     */
    public static final PVector darkPurple = rgb(118, 42, 131);
    public static final PVector purple = rgb(175, 141, 195);
    public static final PVector brightPurple = rgb(231, 212, 232);
    public static final PVector white = rgb(247, 247, 247);
    public static final PVector brightGreen = rgb(217, 240, 211);
    public static final PVector green = rgb(127, 191, 123);
    public static final PVector darkGreen = rgb(27, 120, 55);
            
    /**
     * Get the color for a given value.
     */
    public static PVector color(float value) {
        PVector result;
        
        // Purple range.
        if(value < 0) {
            // Dark.
            if(value <= -5) {
                result = darkPurple;
            }
            // Normal.
            else if(value <= -2.5) {
                result = purple;
            }
            // Bright.
            else {
                result = brightPurple;
            }
        }
        // Green range.
        else if(value > 0) {
            // Dark.
            if(value >= 5) {
                result = darkGreen;
            }
            // Normal.
            else if(value >= 2.5) {
                result = green;
            }
            // Bright.
            else {
                result = brightGreen;
            }
        }
        // White.
        else {
            result = white;
        }
        
        return result;
    }
    
}

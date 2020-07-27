package org.cwi.examine.internal.graphics;

import java.awt.Color;

/**
 * Color utility.
 */
public class Colors {
    
    // Grey scale of given brightness.
    public static Color grey(double brightness) {
        return new Color((float) brightness, (float) brightness, (float) brightness);
    }
    
}

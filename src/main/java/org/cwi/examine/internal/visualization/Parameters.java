package org.cwi.examine.internal.visualization;

import static org.cwi.examine.internal.graphics.StaticGraphics.*;

// General visualization parameters.
public class Parameters {
    public static final int     SET_LABEL_MAX_WIDTH     = 200;
    public static final int     SET_LABEL_MAX_LINES     = 3;
    public static final double  LABEL_PADDING           = 4;
    public static final double  LABEL_DOUBLE_PADDING    = 2 * LABEL_PADDING;
    public static final double  LABEL_ROUNDING          = 16;
    public static final double  LABEL_MARKER_RADIUS     = 6;
    public static final double  LABEL_BAR_HEIGHT        = 25;
    public static final double  SCORE_MIN_RADIUS        = 3;
    public static final double  MARGIN                  = 15;
    
    public static final double  RIBBON_WIDTH    = 8;
    public static final double  RIBBON_SPACE    = 2;
    public static final double  RIBBON_EXTENT   = RIBBON_WIDTH + RIBBON_SPACE;
    public static final double  LINK_WIDTH      = 3;
    public static final double  NODE_OUTLINE    = 4;
    public static final double  NODE_SPACE      = 2;
    public static final double  NODE_MARGIN     = 0.5 * NODE_OUTLINE + NODE_SPACE;
    public static final int     BUFFER_SEGMENTS = 5;
    public static final int     LINK_SEGMENTS   = 10; //20;
    
    // Margin adjusted scene dimensions.
    public static double sceneHeight() { return sketchHeight() - 2 * MARGIN; }
    public static double sceneWidth() { return sketchWidth() - 2 * MARGIN; }
}

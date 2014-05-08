package org.cytoscape.examine.internal.graphics.draw;

import java.awt.Color;
import java.awt.Font;
import org.cytoscape.examine.internal.graphics.Colors;
import org.cytoscape.examine.internal.signal.Variable;

/**
 * Common parameters.
 */
public class Parameters {
    
    // Duration of a full presence transition in seconds.
    public static final Variable<Double> presenceDuration = new Variable<Double>(0.5);
    
    // Interpolation of values transition.
    public static final Variable<Double> moveDuration = new Variable<Double>(0.6);
    
    // Bounding radius for closest element to cursor.
    public static final Variable<Integer> cursorRadius = new Variable<Integer>(15);
    
    // Background color.
    public static final Variable<Color> backgroundColor = new Variable<Color>(Colors.grey(1f));
    
    // Base containment color.
    public static final Variable<Color> containmentColor = new Variable<Color>(Colors.grey(0.25f));
    
    // Text base font.
    public static final Variable<Font> font = new Variable<Font>(null);
    
    // Label font.
    public static final Variable<Font> labelFont = new Variable<Font>(null);
    
    // Footnote font.
    public static final Variable<Font> noteFont = new Variable<Font>(null);
    
    // Text base color.
    public static final Variable<Color> textColor = new Variable<Color>(Colors.grey(0.25f));
    
    // Text base highlight color.
    public static final Variable<Color> textHighlightColor = new Variable<Color>(Colors.grey(0.15f));
    
    // Text base hover color.
    public static final Variable<Color> textHoverColor =
                                            new Variable<Color>(Colors.grey(0f));
    
    // Text contained color.
    public static final Variable<Color> textContainedColor =
                                            new Variable<Color>(Colors.grey(0.8f));
    
    // Text contained highlight color.
    public static final Variable<Color> textContainedHighlightColor =
                                            new Variable<Color>(Colors.grey(0.9f));
    
    // Text contained hover color.
    public static final Variable<Color> textContainedHoverColor =
                                            new Variable<Color>(Colors.grey(1f));
    
    // Visual element spacing.
    public static final Variable<Double> spacing = new Variable<Double>(8.0);
    
}

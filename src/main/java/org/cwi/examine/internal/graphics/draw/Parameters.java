package org.cwi.examine.internal.graphics.draw;

import java.awt.Color;
import java.awt.Font;
import org.cwi.examine.internal.graphics.Colors;

public class Parameters {
    public static double presenceDuration = 0.5;                // Duration of a full presence transition in seconds.
    public static double moveDuration = 0.6;                    // Interpolation of values transition.

    public static int cursorRadius = 15;                        // Bounding radius for closest element to cursor. 
    public static Color backgroundColor = Colors.grey(1f);      // Background color.
    public static Color containmentColor = Colors.grey(0.25f);  // Base containment color.
    
    public static Font font = null;                                         // Text base font.
    public static Font labelFont = null;                                    // Label font.
    public static Font noteFont = null;                                     // Footnote font.
    public static Color textColor = Colors.grey(0.25f);                     // Text base color.
    public static Color textHighlightColor = Colors.grey(0.15f);            // Text base highlight color.
    public static Color textHoverColor = Colors.grey(0f);                   // Text base hover color.
    public static Color textContainedColor = Colors.grey(0.8f);             // Text contained color.
    public static Color textContainedHighlightColor = Colors.grey(0.9f);    // Text contained highlight color.
    public static Color textContainedHoverColor = Colors.grey(1f);          // Text contained hover color.

    public static double spacing = 8.0; // Visual element spacing.
}

package org.cytoscape.examine.internal.graphics.draw;

import org.cytoscape.examine.internal.graphics.AnimatedGraphics;
import org.cytoscape.examine.internal.graphics.PVector;

/**
 * Snippet with a topLeft position and dimension (spanned space).
 */
public abstract class PositionedSnippet extends Snippet {

    // Top left position.
    protected PVector topLeft;
    
    /**
     * Base constructor.
     */
    public PositionedSnippet() {
        this.topLeft = new PVector();
    }

    /**
     * Get top left position.
     */
    public PVector topLeft() {
        return topLeft;
    }
    
    /**
     * Set top left position.
     */
    public void topLeft(PVector newTopLeft) {
        topLeft = newTopLeft;
    }
    
    /**
     * Dimensions (panned space).
     */
    public abstract PVector dimensions(AnimatedGraphics graphics);
    
    /**
     * Top right position. Derived from top left and dimensions.
     */
    public PVector topRight(AnimatedGraphics graphics) {
        return PVector.v(topLeft.x + dimensions(graphics).x, topLeft.y);
    }
    
}

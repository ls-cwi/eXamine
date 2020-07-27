package org.cwi.examine.internal.graphics.draw;

import org.cwi.examine.internal.graphics.PVector;

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
    public abstract PVector dimensions();
    
    /**
     * Top right position. Derived from top left and dimensions.
     */
    public PVector topRight() {
        return PVector.v(topLeft.x + dimensions().x, topLeft.y);
    }
    
}

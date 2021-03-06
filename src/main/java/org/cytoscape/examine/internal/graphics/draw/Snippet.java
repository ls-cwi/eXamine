package org.cytoscape.examine.internal.graphics.draw;

import org.cytoscape.examine.internal.graphics.AnimatedGraphics;
import org.cytoscape.examine.internal.graphics.DrawManager.SnippetValues;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Snippet of visual code that includes:
 * - Geometry interpolation;
 * - Click and hover detection.
 */
public abstract class Snippet {
    
    // Cached snippet values.
    public SnippetValues values = null;
    
    /**
     * Draw method has to be implemented, all Processing
     * commands can be used but should be consistent over
     * multiple calls.
     */
    public abstract void draw(AnimatedGraphics graphics);
    
    public String toolTipText() {
        return null;
    };
    
    /**
     * Called on transition from not-hovered to hovered.
     * Free to override.
     */
    public void beginHovered() {
        
    }
    
    /**
     * Called on transition from hovered to not-hovered.
     * Free to override.
     */
    public void endHovered() {
        
    }
    
    /**
     * Whether snippet is being hovered.
//     */
//    public boolean isHovered() {
//        return equals(hovered());
//    }
    
    /**
     * Default processing event, only called
     * when this snippet is being hovered.
     */
    public void mousePressed(MouseEvent e) {
        
    }

    public void mouseReleased(MouseEvent e) {
        
    }

    public void mouseClicked(MouseEvent e) {
        
    }

    public void mouseDragged(MouseEvent e) {
        
    }

    public void mouseMoved(MouseEvent e) {
        
    }

    public void mouseWheel(int rotation) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed() {
    }

    public void keyReleased() {      
    }

    public void keyTyped() {
    }
    
}

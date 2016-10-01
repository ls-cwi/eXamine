package org.cwi.examine.internal.graphics;

import java.awt.Cursor;
import java.awt.Graphics2D;

import org.cwi.examine.internal.graphics.draw.Parameters;
import org.cwi.examine.internal.graphics.draw.Snippet;
import java.awt.geom.AffineTransform;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.cwi.examine.internal.graphics.draw.PickingGraphics2D;

// Draw and animation manager.
public class DrawManager {
    
    // Parent application.
    private Application parent;
    
    // Time point of last global draw.
    private long oT;
    
    // Time difference since last global draw.
    private double dT;
    
    // Mapping of snippets to managed values.
    private HashMap<Snippet, SnippetValues> snippets;
    
    // Default graphics buffer.
    protected Graphics2D defaultGraphics;
    
    // Picking graphics buffer.
    private PickingGraphics2D pickingGraphics;
    
    // XProcessing graphics to delegate to for drawn snippet.
    protected Graphics2D pg;
    
    // Whether coordinate and color values are being
    // transitioned for drawn snippet.
    protected boolean transitioning;
    
    // Index counter of transitioned values for drawn snippet.
    protected int ti;
    
    // Managed values for drawn snippet.
    protected SnippetValues snippetValues;
    
    // Hovered snippet.
    public Snippet hovered;
    
    // Post screen phase flag.
    private boolean postScreen;
    
    // Style and transform stacks.
    protected ArrayList<Style> styleStack;
    protected ArrayList<AffineTransform> transformStack;
    
    /**
     * Base constructor.
     */
    public DrawManager(Application parent) {
        this.parent = parent;
        this.snippets = new HashMap<Snippet, SnippetValues>(10000);
        this.styleStack = new ArrayList<Style>();
        this.transformStack = new ArrayList<AffineTransform>();
        
        // Get Draw context focus.
        StaticGraphics.dm = this;
        StaticGraphics.application = parent;
    }
    
    /**
     * Install new picking buffer.
     */
    public void updatePickingBuffer() {
        pickingGraphics = new PickingGraphics2D(
                defaultGraphics, this.parent.getWidth(), this.parent.getHeight());
    }
    
    /**
     * Global pre draw.
     */
    public void pre() {
        // Determine the time passed since last draw.        
        long nT = System.currentTimeMillis();
        dT = min(0.1f, oT == 0 ? 0.1f : (double) (nT - oT) / 1000f);
        oT = nT;
        
        // Update default graphics buffer.
        pg = defaultGraphics;
        
        // No parameter interpolation outside of snippets.
        transitioning = false;
        
        // Not in post screen phase.
        postScreen = false;
    }
    
    /**
     * Global post draw.
     */
    public void post() {
        // Update default graphics buffer.
        pg = defaultGraphics;
    }
    
    public void preScreen() {
        pg = defaultGraphics;
        
        // Clear stacks.
        styleStack.clear();
        transformStack.clear();
        
        // Set snippets to not drawn.
        for(SnippetValues v: snippets.values()) {
            v.drawn = false;
        }
    }
    
    /**
     * Fix: recurrent call.
     */
    public void postScreen() {
        // Entering post screenphase.
        postScreen = true;
        
        // Fade away and/or remove redundant snippets.
        Iterator<Entry<Snippet, SnippetValues>> rIt = snippets.entrySet().iterator();
        while(rIt.hasNext()) {
            Entry<Snippet, SnippetValues> r = rIt.next();
            SnippetValues sv = r.getValue();
            
            // Fade in.
            if(sv.drawn) {
                sv.presence = Math.min(1f, sv.presence + dT / Parameters.presenceDuration);
            }
            // Fade out and remove.
            else {
                // Remove completely.
                if(sv.presence < 0.1f) {
                    rIt.remove();
                }
                // Fade out.
                else {
                    sv.presence -= dT / Parameters.presenceDuration;
                }
            }
        }
        
        // Draw non-drawn snippets.
        StaticGraphics.pushTransform();
        StaticGraphics.pushStyle();
        for(Entry<Snippet, SnippetValues> r: snippets.entrySet()) {
            Snippet s = r.getKey();
            SnippetValues sv = r.getValue();
            
            if(!sv.drawn) {
                // Apply last known transformation and style.
                StaticGraphics.dm.pg.setTransform(sv.transform);
                StaticGraphics.style(sv.style);

                // Draw snippet, push to back.
                snippet(s);
            }
        }
        StaticGraphics.popStyle();
        StaticGraphics.popTransform();
    }
    
    public void prePicking() {
        pg = pickingGraphics;
        
        // Clear stacks.
        styleStack.clear();
        transformStack.clear();
        
        // Set snippets to not drawn and assign picking ids.
        int snippetId = PickingGraphics2D.firstId;
        for(SnippetValues v: snippets.values()) {
            v.drawn = false;
            v.id = snippetId;
            snippetId++;
        }
        
        //beginDraw();
        
        // Initialize picking buffer.
        pickingGraphics.preDraw();
    }
    
    public void postPicking() {
        //pg.endDraw();
        
        // Determine object under mouse.
        int pickId = pickingGraphics.closestSnippetId(parent.mouseX, parent.mouseY);
        
        // Remove redundant snippets and determine hovered snippet.
        Snippet oldHovered = hovered;
        hovered = null;
        Iterator<Entry<Snippet, SnippetValues>> rIt = snippets.entrySet().iterator();
        while(rIt.hasNext()) {
            Entry<Snippet, SnippetValues> r = rIt.next();
            if(r.getValue().id == pickId) {
                hovered = r.getKey();
            }
        }
        
        // Call hover transition methods on snippet.
        if(hovered != oldHovered) {
            if(oldHovered != null) {
                oldHovered.endHovered();
            }
            
            if(hovered != null) {
                hovered.beginHovered();
            }
        }
        
        // Adapt cursor to hover.
        StaticGraphics.cursor(hovered == null ?
                Cursor.getDefaultCursor() :
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    /**
     * Draw a snippet.
     */
    public void snippet(Snippet s) {        
        // Store current state (for nested snippets).
        boolean oldTransitioning = transitioning;
        SnippetValues oldSnippetValues = snippetValues;
        int oldTi = ti;
        
        // Set snippet values context.
        // Only extract values in postScreen draws.
        if(postScreen) {
            //snippetValues = s.values;
            snippetValues = snippets.get(s);
            
            // Avoid drawing just added snippets that have not been drawn.
            if(snippetValues == null) {
                return;
            }
        }
        // Register snippet if required and update key.
        else {
            snippetValues = snippets.remove(s);
            if(snippetValues == null) {
                snippetValues = new SnippetValues();
            }
            snippets.put(s, snippetValues);
        }
        
        // Prevent duplicate draw.
        if(!snippetValues.drawn) {
            // Is drawn.
            snippetValues.drawn = true;

            // Parameter interpolation enabled by default.
            transitioning = true;

            // Reset interpolated value counter.
            ti = 0;

            // Push transformation matrix and style (isolate snippet).
            StaticGraphics.pushTransform();
            StaticGraphics.pushStyle();

            // For screen draw, prevent duplicate draw for fading out snippets.
            if(pg == defaultGraphics) {
                // Store last know transformation matrix and style.
                snippetValues.transform.setTransform(StaticGraphics.dm.pg.getTransform());
                snippetValues.style = StaticGraphics.getStyle();

                // Draw.
                s.draw();
            }
            // For picking draw.
            else {
                // Draw in picking buffer.
                pickingGraphics.snippetId = snippetValues.id;

                // Draw.
                s.draw();
            }

            // Pop transformation matrix and style.
            StaticGraphics.popStyle();
            StaticGraphics.popTransform();
        }
        
        // Restore old state (for nested snippets).
        ti = oldTi;
        snippetValues = oldSnippetValues;
        transitioning = oldTransitioning;
        StaticGraphics.noPicking();
    }
    
    protected double mD = 0.5f * Parameters.moveDuration;
    
    // Additional information that is maintained for a snippet during its lifespan.
    public class SnippetValues {
        
        // Whether snippet has been drawn (for fade-out).
        protected boolean drawn = false;
        
        // Last used transformation matrix.
        protected AffineTransform transform = new AffineTransform();
                
        // Last used style.
        protected Style style = new Style();
        
        // Picking number in context of a single draw.
        protected int id;
        
        // Extent of presence in the scene, includes delay.
        protected double presence = -Parameters.moveDuration / Parameters.presenceDuration;
        
        // Intermediate state of doubles that are transitioned over.
        private Intermediate[] intermediates = new Intermediate[20];
        
        // Transition value to target, returns intermediate.
        protected double transition(int index, double target) {            
            // Add value entry.
            if(intermediates.length <= index) {
                intermediates = Arrays.copyOf(intermediates, intermediates.length * 2);
            }
            
            Intermediate im;
            if(intermediates[index] == null) {
                im = new Intermediate();
                im.value = target;
                im.change = 0;
                intermediates[index] = im;
            } else {
                im = intermediates[index];
            }
            
            // Update change, fast initial movement, slow down at end.
            // Do not transition when snippets are fading.
            if(pg == defaultGraphics) {
                if(transitioning) {
                    double d = target - im.value;

                    // Apply acceleration.
                    im.change += dT * (d - (2 * im.change * mD)) / (mD * mD);

                    // Apply velocity.
                    double ad = dT * im.change;
                    im.value += ad;
                    /*if(ad > 0) {
                        im.value = min(target, im.value + ad);
                    } else if(ad < 0) {
                        im.value = max(target, im.value + ad);
                    }*/
                } else {
                    im.value = target;
                }
            }
            
            return im.value;
        }
        
        private class Intermediate {
            double value, change;
        }
    }
    
}

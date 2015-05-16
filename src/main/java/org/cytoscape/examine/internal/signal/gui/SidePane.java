package org.cytoscape.examine.internal.signal.gui;

import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import static java.lang.Math.*;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.*;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import org.cytoscape.examine.internal.graphics.draw.Snippet;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.cytoscape.examine.internal.graphics.PVector;

/**
 * Pane at the left and bar at the the top that give
 * access to most GUI options.
 */
public class SidePane extends Snippet {
    
    // Root category.
    private Category root;
    
    // Category trail.
    private List<Category> categoryTrail;
    
    // Left pane width.
    private double paneWidth;
    
    /**
     * Base constructor.
     */
    public SidePane() {
        this.root = new Category("Options", null);
        this.categoryTrail = new ArrayList<Category>();
        this.paneWidth = 0f;
    }
    
    /**
     * Get root category.
     */
    public Category root() {
        return root;
    }
    
    /**
     * Get left pane width.
     */
    public double paneWidth() {
        return paneWidth;
    }
    
    /**
     * Trail end children (sub category navigation options).
     */
    private List<Category> nextChildren() {
        return categoryTrail.isEmpty() ?
                new ArrayList<Category>() :
                categoryTrail.get(categoryTrail.size() - 1).children;
    }
    
    /**
     * Active bounded representations.
     */
    private List<Representation> representations() {
        return categoryTrail.isEmpty() ?
                new ArrayList<Representation>() :
                categoryTrail.get(categoryTrail.size() - 1).representations;
    }

    @Override
    public void draw() {
        // Update layout for each draw.
        updateLayout();
        
        // Draw pane backdrop.
        color(containmentColor);
        fillRect(0, 0, paneWidth, applicationHeight());
        
        // Otherwise, draw full pane.
        if(!categoryTrail.isEmpty()) {
            // Draw category trail.
            for(Category c: categoryTrail) {
                snippet(c);
            }
            
            // Draw trail end children as navigation options.
            for(Category c: nextChildren()) {
                snippet(c);
            }
            
            // Draw representations of last trail category.
            for(Representation r: representations()) {
                snippet(r);
            }
        }
    }
    
    // Update layout.
    final void updateLayout() {
        double space = spacing;
        
        // Only root.
        if(categoryTrail.isEmpty()) {
            root.position = PVector.v(space, 0);
        }
        // Otherwise, full pane.
        else {
            // Stack category trail, put space to left, top, and below categories.
            PVector p = PVector.v(space, space);
            for(Category c: categoryTrail) {
                c.position = p;
                p = PVector.add(p, PVector.v(0, c.height() + space));
            }
            
            // Indent for next block.
            p = PVector.add(p, PVector.v(2f * space, 0));
                
            // Children of trail end for navigation.
            for(Category c: nextChildren()) {
                c.position = p;
                p = PVector.add(p, PVector.v(0, c.height() + space));
            }
            
            // Space for next block.
            PVector repTopLeft = PVector.add(p, PVector.v(0, 2 * space));
            
            // Fill left over space from top to bottom, left to right.
            double repWidth = 0;
            Iterator<Representation> rIt = representations().iterator();
            PVector repPos = repTopLeft;
            while(rIt.hasNext()) {
                Representation r = rIt.next();
                
                // Move to next column when there is too little height.
                if(repPos.y + r.dimensions().y > sketchHeight()) {
                    repTopLeft = PVector.add(repTopLeft, PVector.v(repWidth + 2 * space, 0));
                    repWidth = 0;
                    repPos = repTopLeft;
                }
                
                // Assign position.
                r.topLeft(repPos);
                
                // Move to next row.
                repPos = PVector.add(repPos, PVector.v(0, r.dimensions().y + 2 * space));
                repWidth = max(repWidth, r.dimensions().x);
            }
        }
        
        // Update spanned width.
        paneWidth = 0;
        if(!categoryTrail.isEmpty()) {
            for(Category c: categoryTrail) {
                paneWidth = max(paneWidth, c.position.x + c.width());
            }
            for(Category c: nextChildren()) {
                paneWidth = max(paneWidth, c.position.x + c.width());
            }
            for(Representation r: representations()) {
                paneWidth = max(paneWidth, r.topLeft().x + r.dimensions().x);
            }
            paneWidth += space;
        }
    }
    
    // Active the given category, closing incompatible categories of the trail.
    public void activate(Category category) {
        // Deactive entire trail.
        for(Category c: categoryTrail) {
            c.active = false;
        }
        categoryTrail.clear();
        
        // Reactive categories up to and including given category.
        while(category != null) {
            category.active = true;
            categoryTrail.add(category);
            category = category.parent;
        }
        Collections.reverse(categoryTrail);
    }
    
    // Deactivate the given category, closing any subsequent category of the trail.
    public void deactivate(Category category) {
        // Category has to be in the trail.
        if(category.active) {
            // Remove tail of trail until given category has been removed.
            Category last;
            do {
                last = categoryTrail.get(categoryTrail.size() - 1);
                last.active = false;
                categoryTrail.remove(last);
            } while(last != category);
        }
    }
    
    // Category of GUI elements.
    public class Category extends Snippet implements Comparable<Category> {
        
        // Name.
        public final String name;
        
        // Parent category.
        public final Category parent;
        
        // Sub categories.
        private List<Category> children;
        
        // Contained GUI elements.
        public final List<Representation> representations;
        
        // Position.
        PVector position;
        
        // Whether category is active.
        public boolean active;
        
        public Category(String name, Category parent) {
            this.name = name;
            this.parent = parent;
            this.children = new ArrayList<Category>();
            this.representations = new ArrayList<Representation>();
            this.position = PVector.v();
            this.active = false;
            
            // Add to children of parent.
            if(!isRoot()) {
                parent.children.add(this);
                Collections.sort(parent.children);
            }
        }
        
        public String name() {
            return name;
        }
        
        // Get sub categories.
        public List<Category> children() {
            return Collections.unmodifiableList(children);
        }
        
        // Create new category as a child of this category.
        public Category add(String name) {
            return new Category(name, this);
        }
        
        // Whether category is root.
        public final boolean isRoot() {
            return parent == null;
        }
        
        // Remove this category.
        public void remove() {
            parent.children.remove(this);
        }

        // Order categories by name, lexicographically.
        @Override
        public int compareTo(Category that) {
            return this.name.compareTo(that.name);
        }

        @Override
        public void draw() {
            // Use base font.
            textFont(font);
            
            double arrowSize = textHeight() / 4f;
            double middle = textMiddle();
                        
            picking();
            
            translate(position);
            
            color(isHovered() ? textContainedHoverColor :
                               active ? textContainedHighlightColor :
                                        textContainedColor);
            
            // State arrow.
            pushTransform();
            translate(arrowSize, middle);
            rotate(active ? (double) PI / 2f : 0);
            
            Path2D triangle = new Path2D.Double();
            triangle.moveTo(-arrowSize, -arrowSize);
            triangle.lineTo(arrowSize, 0);
            triangle.lineTo(-arrowSize, arrowSize);
            triangle.closePath();
            fill(triangle);
            
            popTransform();
            
            translate(textHeight() / 2f + spacing, 0);
            
            // Label.
            text(name);
        }
        
        // Get width.
        public double width() {
            // Use base font.
            textFont(font);
            
            return textWidth(name) + textHeight() / 2f + spacing;
        }
        
        // Get height.
        public double height() {
            // Use base font.
            textFont(font);
            
            return textHeight();
        }

        // (De-)Activate on mouse click.
        @Override
        public void mouseClicked(MouseEvent e) {
            if(active) {
                deactivate(this);
            } else {
                activate(this);
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
        
    }
    
}

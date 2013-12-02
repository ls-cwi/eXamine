package org.cytoscape.examine.internal.visualization;

import static aether.Aether.*;
import static aether.Math.*;
import aether.draw.Layout;
import static aether.draw.Parameters.*;
import aether.draw.Representation;
import java.util.List;
import processing.core.PVector;
import static processing.core.PVector.*;

/**
 * Visual list of significantly expressed GO terms of a specific domain.
 */
public class SetList<E> extends Representation<E> {
    
    // Set label representations.
    private List<SetLabel> labels;
    
    /**
     * Base constructor.
     */
    public SetList(E element, List<SetLabel> labels) {
        super(element);
        
        this.labels = labels;
    }

    @Override
    public PVector dimensions() {
        float space = aether.draw.Parameters.spacing.get();
        PVector domainBounds = v(textWidth(element.toString()), textHeight() + space);
        PVector termBounds = Layout.bounds(labels);
        
        return v(Math.max(domainBounds.x, termBounds.x),
                          domainBounds.y + termBounds.y);
    }

    @Override
    public void draw() {
        float space = spacing.get();
        
        // Domain label (large and heavy).
        pushMatrix();
        translate(topLeft);
        
        textFont(font.get());
        fill(textColor.get());
        text(element.toString());
        
        popMatrix();
        PVector domainBounds = v(textWidth(element.toString()), textHeight() + space);
        
        // Layout term labels.
        PVector termPos = add(topLeft, Y(domainBounds));
        Layout.placeBelowLeft(termPos, labels, 0);
        snippets(labels);
    }
    
}

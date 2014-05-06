package org.cytoscape.examine.internal.signal.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import static org.cytoscape.examine.internal.graphics.Math.*;
import static java.lang.Math.*;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.*;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import org.cytoscape.examine.internal.graphics.draw.Snippet;
import org.cytoscape.examine.internal.signal.Variable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cytoscape.examine.internal.graphics.PVector;

/**
 * Facilitates the interactive manipulation of a variable,
 * constrained to a small list of given values.
 */
public class ChoiceRepresentation<E> extends Representation<Variable<E>> {

    // User friendly name.
    public final String label;
    
    // Whether this representation is contained.
    public final boolean contained;
    
    // Possible values.
    private final List<E> values;
    
    // Value snippets.
    private final List<ValueSnippet> valueSnippets;
    
    /**
     * Base constructor.
     */
    public ChoiceRepresentation(Variable<E> variable,
                                String label,
                                boolean contained,
                                E... values) {
        super(variable);
        
        this.label = label;
        this.contained = contained;
        this.values = Arrays.asList(values);
        this.valueSnippets = new ArrayList<ValueSnippet>();
        for(E value: values) {
            valueSnippets.add(new ValueSnippet(value));
        }
    }
    
    /**
     * Get the values that the variable is constrained to.
     */
    public List<E> values() {
        return Collections.unmodifiableList(values);
    }
    
    @Override
    public void draw() {
        translate(topLeft);
        
        double space = spacing.get();
        
        // Label, use base font.
        textFont(font.get());
        color(contained ? textContainedColor.get() : textColor.get());
        text(label);
        
        // Next line and ident.
        translate(space + dotSize() + space, textHeight() + space);
        
        // Place selection dot.
        int dotIndex = values.indexOf(element.get());
        if(dotIndex >= 0) {
            color(contained ? textContainedHighlightColor.get() : textHighlightColor.get());
            drawEllipse(-(space + dotSize()) / 2f,
                    dotIndex * textHeight() + textMiddle(),
                    dotSize(),
                    dotSize());
        }
        
        // Value labels.
        for(ValueSnippet snippet: valueSnippets) {
            snippet(snippet);
            translate(0, textHeight());
        }
    }
    
    public double dotSize() {
        // Use label font.
        textFont(labelFont.get());
        
        return textHeight() / 2f;
    }

    @Override
    public PVector dimensions() {
        double space = spacing.get() / 2f;
        
        textFont(font.get());
        double width = textWidth(label);
        
        textFont(labelFont.get());
        for(E value: values) {
            width = max(width,
                        textWidth(value.toString()) + dotSize() + 2f * space);
        }
        
        return v(width, textHeight() + space + values.size() * textHeight());
    }
    
    
    /**
     * Value snippet.
     */
    private class ValueSnippet extends Snippet {
        
        // Wrapped value.
        public final E value;

        /**
         * Base constructor.
         */
        public ValueSnippet(E value) {
            this.value = value;
        }

        @Override
        public void draw() {
            picking();
            
            boolean selected = element.get().equals(value);
            Color color = contained ? (isHovered() ? textContainedHoverColor.get() :
                                             selected ? textContainedHighlightColor.get() :
                                                        textContainedColor.get()) :
                                        (isHovered() ? textHoverColor.get() :
                                             selected ? textHighlightColor.get() :
                                                        textColor.get());
                                                        
            // Value label.
            textFont(labelFont.get());
            color(color);
            text(value.toString());
        }
    
        /**
         * Switch variable state on click.
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            element.set(value);
        }
        
    }
    
}

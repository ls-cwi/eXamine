package org.cytoscape.examine.internal.signal.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import org.cytoscape.examine.internal.graphics.PVector;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.*;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import org.cytoscape.examine.internal.signal.Variable;

// Facilitates the interactive manipulation of a boolean variable.
public class BooleanRepresentation extends Representation<Variable<Boolean>> {
    public final double strokeWeight = 1.75f;   // Stroke weight.
    public final String label;                  // User friendly name.
    
    public BooleanRepresentation(Variable<Boolean> variable, String label) {
        super(variable);
        
        this.label = label;
    }
    
    @Override
    public void draw() {
        translate(topLeft);
        
        // Use label font.
        textFont(labelFont);
        
        boolean selected = element.get();
        Color color = isHovered() ? textContainedHoverColor :
                                      selected ? textContainedHighlightColor :
                                                 textContainedColor;
        
        picking();
        
        // Dot; filled when selected, empty otherwise.
        color(color, selected ? 1f : 0f);
        fillEllipse(0.5f * dotSize(), textMiddle(), dotSize(), dotSize());
        color(color);
        strokeWeight(strokeWeight);
        
        // Label.
        color(color);
        translate(dotSize() + space(), 0);
        text(label);
    }
    
    public double dotSize() {
        // Use label font.
        textFont(labelFont);
        
        return textHeight() / 2f - strokeWeight;
    }
    
    public double space() {
        return 0.5f * spacing;
    }

    @Override
    public PVector dimensions() {
        // Use label font.
        textFont(labelFont);
        
        return PVector.v(dotSize() + space() + textWidth(label), textHeight());
    }
    
    // Switch variable state on click.
    @Override
    public void mouseClicked(MouseEvent e) {
        element.set(!element.get());
    }
}

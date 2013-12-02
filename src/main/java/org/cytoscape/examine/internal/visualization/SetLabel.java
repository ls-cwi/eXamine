package org.cytoscape.examine.internal.visualization;

import static aether.Aether.*;
import static aether.Math.*;

import java.text.DecimalFormat;

import aether.color.Color;
import static aether.draw.Parameters.*;

import org.cytoscape.examine.internal.Modules;

import static org.cytoscape.examine.internal.Modules.*;

import org.cytoscape.examine.internal.data.HSet;

import processing.core.PVector;

/**
 * GOTerm set label.
 */
public class SetLabel extends SetRepresentation<HSet> {

    // Maximum number of characters that label shows.
    public final int MAX_CHARACTERS = 40;
    
    // Selected extra space.
    public final float SELECT_SPACE = 5f;
    
    // Text.
    private final String text;

    /**
     * Base constructor.
     */
    public SetLabel(HSet element, String text) {
        super(element);
        
        if(text == null) {
            text = element.toString();
        }
        
        String txt = text.length() >= MAX_CHARACTERS ?
                        text.substring(0, MAX_CHARACTERS) + "..." :
                        text;
        
    	if (Modules.showScore) {
    		DecimalFormat df = new DecimalFormat("0.0E0");
            txt = df.format(element.score) + "  " + txt;
    	}
        
        this.text = txt;
    }

    @Override
    public PVector dimensions() {
        textFont(labelFont.get());
        
        float selAdd = selected() ? 2f * SELECT_SPACE : 0f;
        return v(textWidth(text),
                 textHeight() + selAdd);
    }

    @Override
    public void draw() {
        boolean hL = highlight();
        boolean selected = selected();
        
        translate(topLeft);
        
        translate(selected ? v(0f, SELECT_SPACE) : v());
        
        // Background bubble.
        fill(hL ? containmentColor.get() :
             selected ? visualization.setColors.color(element) : Color.grey(1f),
             selected || hL ? 1f : 0f);
        PVector dim = dimensions();
        if(hL) {
            rect(-1f, 5f, dim.x + 2f, textHeight() - 2f, 4f);
        } else {
            rect(-2f, 2f, dim.x + 4f, textHeight() + 4f, 6f);
        }

        textFont(labelFont.get());
        fill(hL ? textContainedColor.get() :
             selected ? textHighlightColor.get() : textColor.get());
        
        // Set label.
        picking();
        text(text);
    }

    private boolean selected() { 
        return model.selection.activeSetMap.containsKey(element);
    }
    
}

package org.cytoscape.examine.internal.visualization;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import static org.cytoscape.examine.internal.graphics.Math.*;


import static org.cytoscape.examine.internal.graphics.draw.Parameters.*;
import static org.cytoscape.examine.internal.visualization.Parameters.*;

import org.cytoscape.examine.internal.Modules;

import static org.cytoscape.examine.internal.Modules.*;
import org.cytoscape.examine.internal.graphics.Colors;

import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.graphics.PVector;
import org.cytoscape.examine.internal.graphics.StaticGraphics;

/**
 * GOTerm set label.
 */
public class SetLabel extends SetRepresentation<HSet> {
    
    // Text.
    private final String text;
    private final String[] linedText;
    private final SetText setText;
    
    public boolean opened;
    
    private String shortExponent;

    /**
     * Base constructor.
     */
    public SetLabel(HSet element, String text) {
        super(element);
        
        if(text == null) {
            text = element.toString();
        }
        
        this.opened = false;
        
        ArrayList<String> lines = new ArrayList<String>();
        String[] chunks = text.split(" ");
        lines.add(chunks[0] + " ");
        for(int i = 1; i < chunks.length; i++) {
            String chunk = chunks[i];
            int prevLength = lines.get(lines.size() - 1).length();
            if(prevLength + chunk.length() < LABEL_MAX_CHARACTERS - 1 ||
               lines.size() >= LABEL_MAX_LINES) {
                lines.set(lines.size() - 1,
                          lines.get(lines.size() - 1) + chunk + " ");
            } else {
                lines.add(chunk + " ");
            }
        }
        for(int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);
        lines.set(i,
                  line.length() >= LABEL_MAX_CHARACTERS ?
                        line.substring(0, LABEL_MAX_CHARACTERS) + "." :
                        line
                );
        }
        this.linedText = lines.toArray(new String[]{});
        
        
        String txt = text;
                     //text.length() >= LABEL_MAX_CHARACTERS ?
                     //   text.substring(0, LABEL_MAX_CHARACTERS) + "..." :
                     //   text;
    	if (Modules.showScore) {
            DecimalFormat df = new DecimalFormat("0.0E0");
            txt = df.format(element.score) + "  " + txt;
    	}
        this.text = txt;
        
        this.shortExponent = "-" + Double.toString(exponent(element.score));
        this.shortExponent = shortExponent.substring(0, shortExponent.length() - 2);
        
        this.setText = new SetText(element);
    }

    @Override
    public PVector dimensions() {
        textFont(labelFont.get());
        
        double maxLabelWidth = 0;
        for(String line: linedText) {
            maxLabelWidth = Math.max(maxLabelWidth, textWidth(line));
        }
        
        return v(LABEL_PADDING + 2 * LABEL_MARKER_RADIUS + 2 * LABEL_DOUBLE_PADDING
                 + maxLabelWidth + LABEL_PADDING,
                 linedText.length * textHeight() + LABEL_DOUBLE_PADDING);
    }

    @Override
    public void draw() {
        PVector dim = dimensions();
        boolean hL = highlight();
        
        textFont(labelFont.get());
        
        translate(topLeft);
        
        if(opened) {
            snippet(setText);
        }
        
        // Set marker.
        if(!opened && hL) {
            translate(0, !opened && hL ? 2 * LABEL_MARKER_RADIUS : 0);
        } else if(!opened) {
            translate(0, 0);
        } else {
            translate(LABEL_PADDING + LABEL_MARKER_RADIUS, 0.5 * dim.y);
        }
        
        double maxRadius = 0.5 * textHeight() - 2;
        double minScoreExp = exponent(data.minScore.get());
        double maxScoreExp = exponent(data.maxScore.get());
        double scoreExp = exponent(element.score);
        double normScore = Modules.showScore ?
                            ((scoreExp - maxScoreExp) /
                             (minScoreExp - maxScoreExp)) :
                            1;
        double radius = SCORE_MIN_RADIUS + (maxRadius - SCORE_MIN_RADIUS) * normScore;
        //color(hL ? containmentColor.get() : Color.WHITE);
        color(hL ? containmentColor.get() : Colors.grey(0.7));
        fillEllipse(0, 0, radius, radius);
        
        //color(hL ? Color.WHITE : textColor.get());
        color(Color.WHITE);
        StaticGraphics.strokeWeight(1);
        drawEllipse(0, 0, radius, radius);
    }
    
    private double exponent(double value) {
        String[] formatStrings = new DecimalFormat("0.0E0").format(value).split("E");
        return formatStrings.length > 1 ? Math.abs(Double.valueOf(formatStrings[1])) : 0;
    }

    private boolean selected() { 
        return model.selection.activeSetMap.containsKey(element);
    }

    @Override
    public String toolTipText() {
        return text;
    }
    
    private class SetText extends SetRepresentation<HSet> {
        
        public SetText(HSet element) {
            super(element);
        }

        @Override
        public PVector dimensions() {
            return v();
        }

        @Override
        public void draw() {
            PVector dim = SetLabel.this.dimensions();
            boolean hL = highlight();
            boolean selected = selected();
        
            // Background bubble.
            color(hL ? containmentColor.get() :
                 selected ? visualization.setColors.color(element) : Colors.grey(1f),
                 selected || hL ? 1f : 0f);
            fillRect(0f, 0f, dim.x, dim.y, LABEL_ROUNDING);
            
            // Score label.
            color(hL ? textContainedColor.get() :
                  selected ? textHighlightColor.get() : textColor.get()); 
            
            if(Modules.showScore) {
                textFont(noteFont.get());
                text(shortExponent, 2 * LABEL_MARKER_RADIUS + 3,
                                    0.5 * dim.y - LABEL_MARKER_RADIUS);
            }

            // Set label.
            picking();
            textFont(labelFont.get());
            translate(LABEL_PADDING + 2 * LABEL_MARKER_RADIUS + 2 * LABEL_DOUBLE_PADDING,
                      LABEL_PADDING);
            //text(text);
            for(String line: linedText) {
                text(line);
                translate(0, textHeight());
            }
        }
        
    }
    
}

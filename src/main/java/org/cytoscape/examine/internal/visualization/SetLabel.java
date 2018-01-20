package org.cytoscape.examine.internal.visualization;

import org.cytoscape.examine.internal.data.DataSet;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.graphics.Colors;
import org.cytoscape.examine.internal.graphics.PVector;
import org.cytoscape.examine.internal.graphics.StaticGraphics;
import org.cytoscape.examine.internal.model.Model;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static org.cytoscape.examine.internal.graphics.StaticGraphics.color;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.drawEllipse;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.fillEllipse;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.fillRect;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.picking;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.snippet;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.text;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.textFont;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.textHeight;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.textWidth;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.translate;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.containmentColor;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.labelFont;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.noteFont;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.textColor;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.textContainedColor;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.textHighlightColor;
import static org.cytoscape.examine.internal.visualization.Parameters.LABEL_DOUBLE_PADDING;
import static org.cytoscape.examine.internal.visualization.Parameters.LABEL_MARKER_RADIUS;
import static org.cytoscape.examine.internal.visualization.Parameters.LABEL_PADDING;
import static org.cytoscape.examine.internal.visualization.Parameters.LABEL_ROUNDING;
import static org.cytoscape.examine.internal.visualization.Parameters.SCORE_MIN_RADIUS;
import static org.cytoscape.examine.internal.visualization.Parameters.SET_LABEL_MAX_LINES;
import static org.cytoscape.examine.internal.visualization.Parameters.SET_LABEL_MAX_WIDTH;

// GOTerm set label.
public class SetLabel extends SetRepresentation {
    public boolean opened;

    private final DataSet dataSet;
    private final Model model;
    private final SetColors setColors;
    private final String text;
    private final String[] linedText;
    private final SetText setText;
    private final boolean showScore;
    
    private String shortExponent;
    
    protected SetList parentList;

    public SetLabel(DataSet dataSet, Model model, SetColors setColors, HSet element, String text, boolean showScore) {
        super(model, element);

        this.dataSet = dataSet;
        this.model = model;
        this.setColors = setColors;
        this.showScore = showScore;

        this.opened = false;
        
        if(text == null) {
            text = element.toString();
        }
        
        StaticGraphics.textFont(labelFont);
        String[] words = text.split(" ");
        ArrayList<String> lines = new ArrayList<String>();
        for(int i = 0; i < words.length; i++) {
            String w = words[i];
            
            if(lines.size() < SET_LABEL_MAX_LINES) {
                if (textWidth(w) > SET_LABEL_MAX_WIDTH) {
                    lines.add(w.substring(0, SET_LABEL_MAX_WIDTH / (int) (0.75 * textHeight())) + "...");
                } else if(lines.isEmpty()) {
                    lines.add(w);
                } else {
                    String extW = lines.get(lines.size() - 1) + " " + w;
                    if(textWidth(extW) < SET_LABEL_MAX_WIDTH) {
                        lines.set(lines.size() - 1, extW);
                    } else {
                        lines.add(w);
                    }
                }
            }
        }
        this.linedText = lines.toArray(new String[]{});
        
        String txt = text;
    	if (showScore) {
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
        textFont(labelFont);
        
        return PVector.v(LABEL_PADDING + 2 * LABEL_MARKER_RADIUS + 2 * LABEL_DOUBLE_PADDING
                 + SET_LABEL_MAX_WIDTH
                 + LABEL_PADDING,
                 linedText.length * textHeight() + LABEL_DOUBLE_PADDING);
    }

    @Override
    public void draw() {
        PVector dim = dimensions();
        boolean hL = highlight();
        
        textFont(labelFont);
        
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
        double minScoreExp = exponent(dataSet.minScore.get());
        double maxScoreExp = exponent(dataSet.maxScore.get());
        double scoreExp = exponent(element.score);
        double normScore = showScore ?
                            ((scoreExp - maxScoreExp) /
                             (minScoreExp - maxScoreExp)) :
                            1;
        double radius = SCORE_MIN_RADIUS + (maxRadius - SCORE_MIN_RADIUS) * normScore;
        color(hL ? containmentColor : Colors.grey(0.7));
        fillEllipse(0, 0, radius, radius);
        
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

    // Delegate mouse wheel to parent list for scrolling.
    @Override
    public void mouseWheel(int rotation) {
        if(parentList != null) parentList.mouseWheel(rotation);
    }

    @Override
    public String toolTipText() {
        return text;
    }
    
    private class SetText extends SetRepresentation {
        
        public SetText(HSet element) {
            super(model, element);
        }

        @Override
        public PVector dimensions() {
            return PVector.v();
        }

        @Override
        public void draw() {
            PVector dim = SetLabel.this.dimensions();
            boolean hL = highlight();
            boolean selected = selected();
        
            // Background bubble.
            color(hL ? containmentColor :
                 selected ? setColors.color(element) : Colors.grey(1f),
                 selected || hL ? 1f : 0f);
            fillRect(0f, 0f, dim.x, dim.y, LABEL_ROUNDING);
            
            // Score label.
            color(hL ? textContainedColor :
                  selected ? textHighlightColor : textColor); 
            
            if(showScore) {
                textFont(noteFont);
                text(shortExponent, 2 * LABEL_MARKER_RADIUS + 3,
                                    0.5 * dim.y - LABEL_MARKER_RADIUS);
            }

            // Set label.
            picking();
            textFont(labelFont);
            translate(LABEL_PADDING + 2 * LABEL_MARKER_RADIUS + 2 * LABEL_DOUBLE_PADDING,
                      LABEL_PADDING);
            for(String line: linedText) {
                text(line);
                translate(0, textHeight());
            }
        }

        // Delegate to parent.
        @Override
        public void mouseWheel(int rotation) {
            if(selected()) {
                super.mouseWheel(rotation);
            } else {
                SetLabel.this.mouseWheel(rotation);
            }
        }
    }
}

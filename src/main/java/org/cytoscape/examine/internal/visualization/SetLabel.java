package org.cytoscape.examine.internal.visualization;

import org.cytoscape.examine.internal.data.DataSet;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.graphics.Colors;
import org.cytoscape.examine.internal.graphics.PVector;
import org.cytoscape.examine.internal.graphics.AnimatedGraphics;
import org.cytoscape.examine.internal.model.Model;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static org.cytoscape.examine.internal.graphics.draw.Constants.CONTAINMENT_COLOR;
import static org.cytoscape.examine.internal.graphics.draw.Constants.LABEL_FONT;
import static org.cytoscape.examine.internal.graphics.draw.Constants.NOTE_FONT;
import static org.cytoscape.examine.internal.graphics.draw.Constants.TEXT_COLOR;
import static org.cytoscape.examine.internal.graphics.draw.Constants.TEXT_CONTAINED_COLOR;
import static org.cytoscape.examine.internal.graphics.draw.Constants.TEXT_HIGHLIGHT_COLOR;
import static org.cytoscape.examine.internal.visualization.Constants.LABEL_DOUBLE_PADDING;
import static org.cytoscape.examine.internal.visualization.Constants.LABEL_MARKER_RADIUS;
import static org.cytoscape.examine.internal.visualization.Constants.LABEL_PADDING;
import static org.cytoscape.examine.internal.visualization.Constants.LABEL_ROUNDING;
import static org.cytoscape.examine.internal.visualization.Constants.SCORE_MIN_RADIUS;
import static org.cytoscape.examine.internal.visualization.Constants.SET_LABEL_MAX_LINES;
import static org.cytoscape.examine.internal.visualization.Constants.SET_LABEL_MAX_WIDTH;

// GOTerm set label.
public class SetLabel extends SetRepresentation {
    public boolean opened;

    private final DataSet dataSet;
    private final Model model;
    private final SetColors setColors;
    private final String text;
    private final SetText setText;
    private final boolean showScore;

    private String[] cachedLinedText;
    private double cachedLineHeight;
    
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

    private String[] getLinedText(AnimatedGraphics g) {

        g.textFont(LABEL_FONT);

        // If cache is invalid => update lined text.
        if(cachedLineHeight != g.textHeight()) {

            String[] words = text.split(" ");
            ArrayList<String> lines = new ArrayList<String>();
            for (int i = 0; i < words.length; i++) {
                String w = words[i];

                if (lines.size() < SET_LABEL_MAX_LINES) {
                    if (g.textWidth(w) > SET_LABEL_MAX_WIDTH) {
                        lines.add(w.substring(0, SET_LABEL_MAX_WIDTH / (int) (0.75 * g.textHeight())) + "...");
                    } else if (lines.isEmpty()) {
                        lines.add(w);
                    } else {
                        String extW = lines.get(lines.size() - 1) + " " + w;
                        if (g.textWidth(extW) < SET_LABEL_MAX_WIDTH) {
                            lines.set(lines.size() - 1, extW);
                        } else {
                            lines.add(w);
                        }
                    }
                }
            }

            cachedLinedText = lines.toArray(new String[]{});
            cachedLineHeight = g.textHeight();
        }

        return cachedLinedText;
    }

    @Override
    public PVector dimensions(AnimatedGraphics g) {
        g.textFont(LABEL_FONT);

        return PVector.v(LABEL_PADDING + 2 * LABEL_MARKER_RADIUS + 2 * LABEL_DOUBLE_PADDING
                 + SET_LABEL_MAX_WIDTH
                 + LABEL_PADDING,
                 getLinedText(g).length * g.textHeight() + LABEL_DOUBLE_PADDING);
    }

    @Override
    public void draw(AnimatedGraphics g) {
        PVector dim = dimensions(g);
        boolean hL = highlight();
        
        g.textFont(LABEL_FONT);
        
        g.translate(topLeft);
        
        if(opened) {
            g.snippet(setText);
        }
        
        // Set marker.
        if(!opened && hL) {
            g.translate(0, !opened && hL ? 2 * LABEL_MARKER_RADIUS : 0);
        } else if(!opened) {
            g.translate(0, 0);
        } else {
            g.translate(LABEL_PADDING + LABEL_MARKER_RADIUS, 0.5 * dim.y);
        }
        
        double maxRadius = 0.5 * g.textHeight() - 2;
        double minScoreExp = exponent(dataSet.minScore.get());
        double maxScoreExp = exponent(dataSet.maxScore.get());
        double scoreExp = exponent(element.score);
        double normScore = showScore ?
                            ((scoreExp - maxScoreExp) /
                             (minScoreExp - maxScoreExp)) :
                            1;
        double radius = SCORE_MIN_RADIUS + (maxRadius - SCORE_MIN_RADIUS) * normScore;
        g.color(hL ? CONTAINMENT_COLOR : Colors.grey(0.7));
        g.fillEllipse(0, 0, radius, radius);
        
        g.color(Color.WHITE);
        g.strokeWeight(1);
        g.drawEllipse(0, 0, radius, radius);
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
        public PVector dimensions(AnimatedGraphics g) {
            return PVector.v();
        }

        @Override
        public void draw(AnimatedGraphics g) {
            PVector dim = SetLabel.this.dimensions(g);
            boolean hL = highlight();
            boolean selected = selected();
        
            // Background bubble.
            g.color(hL ? CONTAINMENT_COLOR :
                 selected ? setColors.color(element) : Colors.grey(1f),
                 selected || hL ? 1f : 0f);
            g.fillRect(0f, 0f, dim.x, dim.y, LABEL_ROUNDING);
            
            // Score label.
            g.color(hL ? TEXT_CONTAINED_COLOR :
                  selected ? TEXT_HIGHLIGHT_COLOR : TEXT_COLOR);
            
            if(showScore) {
                g.textFont(NOTE_FONT);
                g.text(shortExponent, 2 * LABEL_MARKER_RADIUS + 3, 0.5 * dim.y - LABEL_MARKER_RADIUS);
            }

            // Set label.
            g.picking();
            g.textFont(LABEL_FONT);
            g.translate(LABEL_PADDING + 2 * LABEL_MARKER_RADIUS + 2 * LABEL_DOUBLE_PADDING,
                      LABEL_PADDING);
            for(String line: getLinedText(g)) {
                g.text(line);
                g.translate(0, g.textHeight());
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

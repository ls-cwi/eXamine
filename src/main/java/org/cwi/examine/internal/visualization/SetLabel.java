package org.cwi.examine.internal.visualization;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.cwi.examine.internal.graphics.PVector;
import org.cwi.examine.internal.data.Network;
import org.cwi.examine.internal.graphics.Colors;
import org.cwi.examine.internal.data.HAnnotation;
import org.cwi.examine.internal.graphics.StaticGraphics;

import static org.cwi.examine.internal.graphics.StaticGraphics.*;

// GOTerm set label.
public class SetLabel extends SetRepresentation {

    public boolean opened;
    private final String text;
    private final String[] linedText;
    private final SetText setText;
    
    private String shortExponent;
    
    protected SetList parentList;

    public SetLabel(final Visualization visualization, final HAnnotation element, String text) {
        super(visualization, element);

        this.opened = false;
        
        if(text == null) {
            text = element.toString();
        }
        
        StaticGraphics.textFont(org.cwi.examine.internal.graphics.draw.Parameters.labelFont);
        String[] words = text.split(" ");
        ArrayList<String> lines = new ArrayList<>();
        for(int i = 0; i < words.length; i++) {
            String w = words[i];
            
            if(lines.size() < Parameters.SET_LABEL_MAX_LINES) {
                if (textWidth(w) > Parameters.SET_LABEL_MAX_WIDTH) {
                    lines.add(w.substring(0, Parameters.SET_LABEL_MAX_WIDTH / (int) (0.75 * textHeight())) + "...");
                } else if(lines.isEmpty()) {
                    lines.add(w);
                } else {
                    String extW = lines.get(lines.size() - 1) + " " + w;
                    if(textWidth(extW) < Parameters.SET_LABEL_MAX_WIDTH) {
                        lines.set(lines.size() - 1, extW);
                    } else {
                        lines.add(w);
                    }
                }
            }
        }
        this.linedText = lines.toArray(new String[]{});
        
        String txt = text;
        DecimalFormat df = new DecimalFormat("0.0E0");
        txt = df.format(element.score) + "  " + txt;
        this.text = txt;
        
        this.shortExponent = "-" + Double.toString(exponent(element.score));
        this.shortExponent = shortExponent.substring(0, shortExponent.length() - 2);
        
        this.setText = new SetText(element);
    }

    @Override
    public PVector dimensions() {
        textFont(org.cwi.examine.internal.graphics.draw.Parameters.labelFont);
        
        return PVector.v(Parameters.LABEL_PADDING + 2 * Parameters.LABEL_MARKER_RADIUS + 2 * Parameters.LABEL_DOUBLE_PADDING
                 + Parameters.SET_LABEL_MAX_WIDTH
                 + Parameters.LABEL_PADDING,
                 linedText.length * textHeight() + Parameters.LABEL_DOUBLE_PADDING);
    }

    @Override
    public void draw() {
        PVector dim = dimensions();
        boolean hL = highlight();
        
        textFont(org.cwi.examine.internal.graphics.draw.Parameters.labelFont);
        
        translate(topLeft);
        
        if(opened) {
            snippet(setText);
        }
        
        // Set marker.
        if(!opened && hL) {
            translate(0, !opened && hL ? 2 * Parameters.LABEL_MARKER_RADIUS : 0);
        } else if(!opened) {
            translate(0, 0);
        } else {
            translate(Parameters.LABEL_PADDING + Parameters.LABEL_MARKER_RADIUS, 0.5 * dim.y);
        }

        final Network network = visualization.model.activeNetwork.get();
        double maxRadius = 0.5 * textHeight() - 2;
        double minScoreExp = exponent(network.minAnnotationScore);
        double maxScoreExp = exponent(network.maxAnnotationScore);
        double scoreExp = exponent(element.score);
        double normScore = (scoreExp - maxScoreExp) / (minScoreExp - maxScoreExp);
        double radius = Parameters.SCORE_MIN_RADIUS + (maxRadius - Parameters.SCORE_MIN_RADIUS) * normScore;
        color(hL ? org.cwi.examine.internal.graphics.draw.Parameters.containmentColor : Colors.grey(0.7));
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
        return visualization.model.selection.activeSetMap.containsKey(element);
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
        
        public SetText(HAnnotation element) {
            super(SetLabel.this.visualization, element);
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
            color(hL ? org.cwi.examine.internal.graphics.draw.Parameters.containmentColor :
                 selected ? visualization.setColors.color(element) : Colors.grey(1f),
                 selected || hL ? 1f : 0f);
            fillRect(0f, 0f, dim.x, dim.y, Parameters.LABEL_ROUNDING);
            
            // Score label.
            color(hL ? org.cwi.examine.internal.graphics.draw.Parameters.textContainedColor :
                  selected ? org.cwi.examine.internal.graphics.draw.Parameters.textHighlightColor : org.cwi.examine.internal.graphics.draw.Parameters.textColor);

            textFont(org.cwi.examine.internal.graphics.draw.Parameters.noteFont);
            text(shortExponent, 2 * Parameters.LABEL_MARKER_RADIUS + 3, 0.5 * dim.y - Parameters.LABEL_MARKER_RADIUS);

            // Set label.
            picking();
            textFont(org.cwi.examine.internal.graphics.draw.Parameters.labelFont);
            translate(Parameters.LABEL_PADDING + 2 * Parameters.LABEL_MARKER_RADIUS + 2 * Parameters.LABEL_DOUBLE_PADDING,
                      Parameters.LABEL_PADDING);
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

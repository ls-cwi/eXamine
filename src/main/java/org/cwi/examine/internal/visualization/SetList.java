package org.cwi.examine.internal.visualization;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;

import org.cwi.examine.internal.data.HCategory;
import org.cwi.examine.internal.graphics.PVector;
import org.cwi.examine.internal.graphics.draw.Layout;
import org.cwi.examine.internal.graphics.draw.Representation;
import java.util.List;

import static org.cwi.examine.internal.graphics.StaticGraphics.*;

// Visual list of significantly expressed GO terms of a specific domain.
public class SetList extends Representation<HCategory> {
    private final Visualization visualization;
    private final List<SetLabel> labels;
    private int positionScroll;                     // Internal set list scroll.
    
    public SetList(final Visualization visualization, HCategory element, List<SetLabel> labels) {
        super(element);

        this.visualization = visualization;
        this.labels = labels;
        this.positionScroll = 0;
        
        for(SetLabel l: labels) {
            l.parentList = this;
        }
    }

    @Override
    public PVector dimensions() {
        PVector dimensions;
        
        textFont(org.cwi.examine.internal.graphics.draw.Parameters.font);
        
        double space = org.cwi.examine.internal.graphics.draw.Parameters.spacing;
        PVector domainBounds = PVector.v(0.75 * textHeight() + textWidth(element.toString()),
                                 textHeight() + space + Parameters.LABEL_BAR_HEIGHT + space);
        
        if(isOpened()) {
            double termHeight = Layout.bounds(labels).y;
            dimensions = PVector.v(Math.max(domainBounds.x, Layout.maxWidth(labels)),
                           domainBounds.y + termHeight);
        } else {
            dimensions = PVector.v(Math.max(domainBounds.x, Layout.maxWidth(labels)), domainBounds.y);
        }
        
        return dimensions;
    }

    @Override
    public void draw() {
        PVector dim = dimensions();
        
        // Category label.
        pushTransform();
        translate(topLeft);
        
        // Background rectangle to enable scrolling.
        picking();
        color(Color.WHITE);
        drawRect(0, 0, dim.x, dim.y);
        
        translate(0, textHeight());
        
        textFont(org.cwi.examine.internal.graphics.draw.Parameters.font);
        color(isOpened() ? org.cwi.examine.internal.graphics.draw.Parameters.textColor: org.cwi.examine.internal.graphics.draw.Parameters.textColor.brighter().brighter());
        text(element.toString(), 0.75 * textHeight(), 0);
        
        // Arrows.
        pushTransform();
        double arrowRad = 0.25 * textHeight();
        double arrowTrunc = 0.25 * 0.85 * textHeight();
        double arrowMargin = 0.33 * arrowTrunc;
        
        translate(arrowRad, -arrowRad);
        rotate(isOpened() ? 0.5 * Math.PI : 0);
        
        Path2D arrows = new Path2D.Double();
        arrows.moveTo(-arrowRad, 0);
        arrows.lineTo(-arrowMargin, -arrowTrunc);
        arrows.lineTo(-arrowMargin, arrowTrunc);
        arrows.closePath();
        arrows.moveTo(arrowRad, 0);
        arrows.lineTo(arrowMargin, arrowTrunc);
        arrows.lineTo(arrowMargin, -arrowTrunc);
        arrows.closePath();
        
        fill(arrows);
        popTransform();
        
        //noPicking();
        
        popTransform();
        
        // Layout tagged set labels.
        List<SetLabel> taggedLabels = new ArrayList<>();    // Tagged set label representations.
        List<SetLabel> remainderLabels = new ArrayList<>(); // Set label representations.
        for(SetLabel lbl: labels) {
            (visualization.model.selection.activeSetMap.containsKey(lbl.element) ?
                    taggedLabels :
                    remainderLabels).add(lbl);
        }
        
        PVector domainBounds = PVector.v(textWidth(element.toString()),
                                 textHeight() + org.cwi.examine.internal.graphics.draw.Parameters.spacing);
        PVector topTaggedPos = PVector.add(topLeft, domainBounds.Y());
        
        PVector labelPos = topTaggedPos;
        for(int i = 0; i < taggedLabels.size(); i++) {
            SetLabel label = taggedLabels.get(i);
            PVector labelDim = label.dimensions();
            
            label.opened = true;
            label.topLeft(labelPos);
            labelPos = PVector.add(labelPos, PVector.v(0, labelDim.y + 2));
        }
        
        // Layout remaining set labels.
        int skipCount = isOpened() ? positionScroll : remainderLabels.size();
        PVector topBarPos = PVector.add(labelPos,
                                        PVector.v(0, taggedLabels.isEmpty() ? 0 : textHeight()));
        PVector topListPos = PVector.add(topBarPos, PVector.v(0,
                    skipCount > 0 ? Parameters.LABEL_BAR_HEIGHT + org.cwi.examine.internal.graphics.draw.Parameters.spacing : 0));
        PVector bottomBarPos = null;
        
        double barIncrement = Math.min(
            2 * Parameters.LABEL_MARKER_RADIUS + 2,
            dim.x / (double) remainderLabels.size()
        );
        
        int i;
        
        // Place in top bar.
        for(i = 0; i < skipCount && i < remainderLabels.size(); i++) {
            SetLabel label = remainderLabels.get(i);
            label.opened = false;
            label.topLeft(PVector.v(topBarPos.x + Parameters.LABEL_MARKER_RADIUS + i * barIncrement,
                            topBarPos.y + Parameters.LABEL_MARKER_RADIUS));
        }
        
        // Place in mid section, as full.
        labelPos = topListPos;
        for(i = skipCount; i < remainderLabels.size(); i++) {
            SetLabel label = remainderLabels.get(i);
            PVector labelDim = label.dimensions();
            
            label.opened =
                topLeft.y + labelPos.y + 2 * labelDim.y +
                Parameters.LABEL_BAR_HEIGHT + org.cwi.examine.internal.graphics.draw.Parameters.spacing < Parameters.sceneHeight();
            
            if(label.opened) {
                label.topLeft(labelPos);
            }
            // Place in bottom bar.
            else {
                if(bottomBarPos == null) {
                    bottomBarPos = labelPos;
                }
                
                label.topLeft(PVector.v(topBarPos.x + Parameters.LABEL_MARKER_RADIUS + i * barIncrement,
                                bottomBarPos.y + 2 * Parameters.LABEL_MARKER_RADIUS));
            }
            
            labelPos = PVector.add(labelPos, PVector.v(0, labelDim.y + 2));
        }
        
        snippets(remainderLabels);
        snippets(taggedLabels);
    }
    
    public boolean isOpened() {
        return visualization.model.openedCategories.get().contains(element);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(isOpened()) {
            visualization.model.openedCategories.remove(element);
        } else {
            visualization.model.openedCategories.add(element);
        }
    }

    @Override
    public void mouseWheel(int rotation) {
        positionScroll = Math.max(0, Math.min(labels.size() - 1, positionScroll + rotation));
    }
}

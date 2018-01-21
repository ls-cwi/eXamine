package org.cytoscape.examine.internal.visualization;

import org.cytoscape.examine.internal.data.HCategory;
import org.cytoscape.examine.internal.graphics.AnimatedGraphics;
import org.cytoscape.examine.internal.graphics.PVector;
import org.cytoscape.examine.internal.graphics.draw.Constants;
import org.cytoscape.examine.internal.graphics.draw.Layout;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import org.cytoscape.examine.internal.model.Model;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import static org.cytoscape.examine.internal.graphics.draw.Constants.FONT;
import static org.cytoscape.examine.internal.graphics.draw.Constants.SPACING;
import static org.cytoscape.examine.internal.graphics.draw.Constants.TEXT_COLOR;
import static org.cytoscape.examine.internal.visualization.Constants.LABEL_BAR_HEIGHT;
import static org.cytoscape.examine.internal.visualization.Constants.LABEL_MARKER_RADIUS;

// Visual list of significantly expressed GO terms of a specific domain.
public class SetList extends Representation<HCategory> {

    private final Model model;
    private final List<SetLabel> labels;
    private int positionScroll;                     // Internal set list scroll.
    
    public SetList(Model model, HCategory element, List<SetLabel> labels) {
        super(element);

        this.model = model;
        this.labels = labels;
        this.positionScroll = 0;
        
        for(SetLabel l: labels) {
            l.parentList = this;
        }
    }

    @Override
    public PVector dimensions(AnimatedGraphics g) {
        PVector dimensions;
        
        g.textFont(FONT);
        
        double space = Constants.SPACING;
        PVector domainBounds = PVector.v(0.75 * g.textHeight() + g.textWidth(element.toString()),
                                 g.textHeight() + space /*+ LABEL_BAR_HEIGHT + space*/);

        final double termHeight;
        if(isOpened()) {
            termHeight = Layout.bounds(g, labels).y;
        } else {
            // Layout tagged set labels.
            List<SetLabel> taggedLabels = new ArrayList<SetLabel>();    // Tagged set label representations.
            for(SetLabel lbl: labels) {
                if (model.selection.activeSetMap.containsKey(lbl.element)) {
                    taggedLabels.add(lbl);
                }
            }

            termHeight = Layout.bounds(g, labels).y;
        }

        dimensions = PVector.v(Math.max(domainBounds.x, Layout.maxWidth(g, labels)), domainBounds.y + termHeight);
        
        return dimensions;
    }

    @Override
    public void draw(AnimatedGraphics g) {
        PVector dim = dimensions(g);
        
        // Category label.
        g.pushTransform();
        g.translate(topLeft);
        
        // Background rectangle to enable scrolling.
        g.picking();
        g.color(Color.WHITE);
        g.drawRect(0, 0, dim.x, dim.y);
        
        g.translate(0, g.textHeight());
        
        g.textFont(FONT);
        g.color(isOpened() ? TEXT_COLOR : TEXT_COLOR.brighter().brighter());
        g.text(element.toString(), g.getDrawManager().isAnimated() ? 0.75 * g.textHeight() : 0, 0);
        
        // Arrows.
        if(g.getDrawManager().isAnimated()) {
            g.pushTransform();
            double arrowRad = 0.25 * g.textHeight();
            double arrowTrunc = 0.25 * 0.85 * g.textHeight();
            double arrowMargin = 0.33 * arrowTrunc;

            g.translate(arrowRad, -arrowRad);
            g.rotate(isOpened() ? 0.5 * Math.PI : 0);

            Path2D arrows = new Path2D.Double();
            arrows.moveTo(-arrowRad, 0);
            arrows.lineTo(-arrowMargin, -arrowTrunc);
            arrows.lineTo(-arrowMargin, arrowTrunc);
            arrows.closePath();
            arrows.moveTo(arrowRad, 0);
            arrows.lineTo(arrowMargin, arrowTrunc);
            arrows.lineTo(arrowMargin, -arrowTrunc);
            arrows.closePath();

            g.fill(arrows);
            g.popTransform();
        }
        
        //noPicking();
        
        g.popTransform();
        
        // Layout tagged set labels.
        List<SetLabel> taggedLabels = new ArrayList<SetLabel>();    // Tagged set label representations.
        List<SetLabel> remainderLabels = new ArrayList<SetLabel>(); // Set label representations.
        for(SetLabel lbl: labels) {
            (model.selection.activeSetMap.containsKey(lbl.element) ?
                    taggedLabels :
                    remainderLabels).add(lbl);
        }
        
        PVector domainBounds = PVector.v(g.textWidth(element.toString()), g.textHeight() + SPACING);
        PVector topTaggedPos = PVector.add(topLeft, domainBounds.Y());
        
        PVector labelPos = topTaggedPos;
        for(int i = 0; i < taggedLabels.size(); i++) {
            SetLabel label = taggedLabels.get(i);
            PVector labelDim = label.dimensions(g);
            
            label.opened = true;
            label.topLeft(labelPos);
            labelPos = PVector.add(labelPos, PVector.v(0, labelDim.y + 2));
        }
        
        // Layout remaining set labels.
        int skipCount = isOpened() ? positionScroll : remainderLabels.size();
        PVector topBarPos = PVector.add(labelPos,
                                        PVector.v(0, taggedLabels.isEmpty() ? 0 : g.textHeight()));
        PVector topListPos = PVector.add(topBarPos, PVector.v(0,
                    skipCount > 0 ? LABEL_BAR_HEIGHT + SPACING : 0));
        PVector bottomBarPos = null;
        
        double barIncrement = Math.min(
            2 * LABEL_MARKER_RADIUS + 2,
            dim.x / (double) remainderLabels.size()
        );
        
        int i;
        
        // Place in top bar.
        for(i = 0; i < skipCount && i < remainderLabels.size(); i++) {
            SetLabel label = remainderLabels.get(i);
            label.opened = false;
            label.topLeft(PVector.v(topBarPos.x + LABEL_MARKER_RADIUS + i * barIncrement,
                            topBarPos.y + LABEL_MARKER_RADIUS));
        }
        
        // Place in mid section, as full.
        labelPos = topListPos;
        for(i = skipCount; i < remainderLabels.size(); i++) {
            SetLabel label = remainderLabels.get(i);
            PVector labelDim = label.dimensions(g);
            
            label.opened =
                topLeft.y + labelPos.y + 2 * labelDim.y +
                LABEL_BAR_HEIGHT + SPACING < g.getCanvasHeight();
            
            if(label.opened) {
                label.topLeft(labelPos);
            }
            // Place in bottom bar.
            else {
                if(bottomBarPos == null) {
                    bottomBarPos = labelPos;
                }
                
                label.topLeft(PVector.v(topBarPos.x + LABEL_MARKER_RADIUS + i * barIncrement,
                                bottomBarPos.y + 2 * LABEL_MARKER_RADIUS));
            }
            
            labelPos = PVector.add(labelPos, PVector.v(0, labelDim.y + 2));
        }
        
        g.snippets(remainderLabels);
        g.snippets(taggedLabels);
    }
    
    public boolean isOpened() {
        return model.openedCategories.get().contains(element);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(isOpened()) {
            model.openedCategories.remove(element);
        } else {
            model.openedCategories.add(element);
        }
    }

    @Override
    public void mouseWheel(int rotation) {
        positionScroll =
                Math.max(0,
                    Math.min(labels.size() - 1,
                             positionScroll + rotation));
    }
}

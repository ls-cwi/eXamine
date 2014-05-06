package org.cytoscape.examine.internal.visualization;

import java.awt.event.MouseEvent;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import static org.cytoscape.examine.internal.graphics.Math.*;
import org.cytoscape.examine.internal.graphics.draw.Layout;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.*;
import static org.cytoscape.examine.internal.visualization.Parameters.*;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import java.util.List;
import org.cytoscape.examine.internal.Modules;
import org.cytoscape.examine.internal.data.HCategory;
import org.cytoscape.examine.internal.graphics.PVector;

/**
 * Visual list of significantly expressed GO terms of a specific domain.
 */
public class SetList extends Representation<HCategory> {
    
    // Set label representations.
    private List<SetLabel> labels;
    
    // Internal set list scroll.
    private int positionScroll;
    
    /**
     * Base constructor.
     */
    public SetList(HCategory element, List<SetLabel> labels) {
        super(element);
        
        this.labels = labels;
        this.positionScroll = 0;
    }

    @Override
    public PVector dimensions() {
        PVector dimensions;
        
        textFont(font.get());
        
        double space = org.cytoscape.examine.internal.graphics.draw.Parameters.spacing.get();
        //PVector domainBounds = v(textWidth(element.toString()), textHeight() + space);
        PVector domainBounds = v(textWidth(element.toString()),
                                 textHeight() + space + LABEL_BAR_HEIGHT + space);
        
        if(isOpened()) {
            PVector termBounds = Layout.bounds(labels);
            dimensions = v(Math.max(domainBounds.x, Layout.maxWidth(labels)),
                           domainBounds.y + termBounds.y);
        } else {
            dimensions = v(Math.max(domainBounds.x, Layout.maxWidth(labels)), domainBounds.y);
        }
        
        return dimensions;
    }

    @Override
    public void draw() {
        double space = spacing.get();
        
        // Category label.
        pushTransform();
        translate(topLeft);
        
        picking();
        textFont(font.get());
        color(isOpened() ? textColor.get() : textColor.get().brighter().brighter());
        text(element.toString());
        noPicking();
        
        popTransform();
        
        // Layout set labels.
        int skipCount = isOpened() ? positionScroll : labels.size();
        PVector dim = dimensions();
        PVector domainBounds = v(textWidth(element.toString()),
                                 textHeight() + space);
        PVector topBarPos = PVector.add(topLeft, domainBounds.Y());
        PVector topListPos = PVector.add(topBarPos, v(0,
                    skipCount > 0 ? LABEL_BAR_HEIGHT + space : 0));
        PVector bottomBarPos = null;
        
        double barIncrement = Math.min(
            2 * LABEL_MARKER_RADIUS + 2,
            dim.x / (double) labels.size()
        );
        
        int i;
        
        // Place in top bar.
        for(i = 0; i < skipCount && i < labels.size(); i++) {
            SetLabel label = labels.get(i);
            label.opened = false;
            label.topLeft(v(topBarPos.x + LABEL_MARKER_RADIUS + i * barIncrement,
                            topBarPos.y + LABEL_MARKER_RADIUS));
        }
        
        // Place in mid section, as full.
        PVector labelPos = topListPos;
        for(i = skipCount; i < labels.size(); i++) {
            SetLabel label = labels.get(i);
            PVector labelDim = label.dimensions();
            
            label.opened =
                topLeft.y + labelPos.y + 2 * labelDim.y +
                LABEL_BAR_HEIGHT + space < sceneHeight();
            
            if(label.opened) {
                label.topLeft(labelPos);
            }
            // Place in bottom bar.
            else {
                if(bottomBarPos == null) {
                    bottomBarPos = labelPos;
                }
                
                label.topLeft(v(topBarPos.x + LABEL_MARKER_RADIUS + i * barIncrement,
                                bottomBarPos.y + 2 * LABEL_MARKER_RADIUS));
            }
            
            labelPos = PVector.add(labelPos, v(0, labelDim.y + 2));
        }
        
        snippets(labels);
        
        // Category sets.
        /*if(isOpened()) {
            PVector domainBounds = v(textWidth(element.toString()), textHeight() + space);

            // Layout term labels.
            PVector termPos = PVector.add(topLeft, domainBounds.Y());
            Layout.placeBelowLeft(termPos, labels, 2);
            snippets(labels);
        }*/
    }
    
    public boolean isOpened() {
        return Modules.model.openedCategories.get().contains(element);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(isOpened()) {
            Modules.model.openedCategories.remove(element);
        } else {
            Modules.model.openedCategories.add(element);
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

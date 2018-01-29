package org.cytoscape.examine.internal.visualization;

import org.cytoscape.examine.internal.data.HCategory;
import org.cytoscape.examine.internal.graphics.AnimatedGraphics;
import org.cytoscape.examine.internal.graphics.PVector;
import org.cytoscape.examine.internal.graphics.draw.Constants;
import org.cytoscape.examine.internal.graphics.draw.Layout;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import org.cytoscape.examine.internal.model.Model;
import org.cytoscape.examine.internal.signal.Observer;

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
    private int positionScroll;


    private final List<SetLabel> taggedLabels = new ArrayList<SetLabel>();
    private final List<SetLabel> remainderLabels = new ArrayList<SetLabel>();

    public SetList(Model model, HCategory element, List<SetLabel> labels) {
        super(element);

        this.model = model;
        this.labels = labels;
        this.positionScroll = 0;

        for (SetLabel l : labels) {
            l.parentList = this;
        }

        model.selection.change.subscribe(new Observer() {
            @Override
            public void signal() {
                updateTaggedRemainderLabels();
            }
        });
        updateTaggedRemainderLabels();
    }

    private void updateTaggedRemainderLabels() {
        taggedLabels.clear();
        remainderLabels.clear();
        for (final SetLabel label : labels) {
            (model.selection.activeSetMap.containsKey(label.element) ? taggedLabels : remainderLabels).add(label);
        }
    }

    @Override
    public PVector dimensions(AnimatedGraphics g) {
        final boolean isAnimated = g.getDrawManager().isAnimated();

        PVector dimensions;

        g.textFont(FONT);

        // Header space.
        double space = Constants.SPACING;
        PVector domainBounds = PVector.v(
                (isAnimated ? 0.75 * g.textHeight() : 0) + g.textWidth(element.toString()),
                g.textHeight() + space
        );

        // Space required by the set tags.
        layoutTaggedLabel(g, domainBounds.Y());
        final PVector labelBounds;
        if (isOpened()) {
            // Factor in additional space for collapsed set tag markers at top and bottom.
            final int skipCount = isOpened() ? positionScroll : remainderLabels.size();
            final PVector tightBounds = Layout.bounds(g, labels);
            labelBounds = PVector.v(tightBounds.x, tightBounds.y + (skipCount > 0 ? LABEL_BAR_HEIGHT + space : 0));
        } else {
            labelBounds = Layout.bounds(g, taggedLabels);
        }

        // Add space at the bottom to separate this list from potential subsequent lists.
        dimensions = PVector.v(
                Math.max(domainBounds.x, Layout.maxWidth(g, labels)),
                domainBounds.y + labelBounds.y + (isAnimated ? LABEL_BAR_HEIGHT + space : 0) + space
        );

        return dimensions;
    }

    private PVector layoutTaggedLabel(final AnimatedGraphics g, final PVector topLeft) {
        PVector labelPos = topLeft;
        for (int i = 0; i < taggedLabels.size(); i++) {
            SetLabel label = taggedLabels.get(i);
            PVector labelDim = label.dimensions(g);

            label.opened = true;
            label.topLeft(labelPos);
            labelPos = PVector.add(labelPos, PVector.v(0, labelDim.y + 2));
        }

        return labelPos;
    }

    @Override
    public void draw(AnimatedGraphics g) {
        final boolean isAnimated = g.getDrawManager().isAnimated();
        PVector dim = dimensions(g);

        // Category label.
        g.pushTransform();
        g.translate(topLeft);

        g.textFont(FONT);

        // Transparent background rectangle to enable scrolling.
        g.picking();
        g.color(Color.BLACK, 0);
        g.fillRect(0, 0, dim.x, dim.y);

        g.translate(0, g.textHeight());

        g.color(isOpened() ? TEXT_COLOR : TEXT_COLOR.brighter().brighter());
        g.text(element.toString(), isAnimated ? 0.75 * g.textHeight() : 0, 0);

        // Arrows.
        if (isAnimated) {
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

        g.popTransform();

        PVector domainBounds = PVector.v(g.textWidth(element.toString()), g.textHeight() + SPACING);
        PVector topTaggedPos = PVector.add(topLeft, domainBounds.Y());

        PVector labelPos = layoutTaggedLabel(g, topTaggedPos);

        g.snippets(taggedLabels);

        // Layout remaining set labels.
        if (isAnimated) {
            int skipCount = isOpened() ? positionScroll : remainderLabels.size();
            PVector topBarPos = PVector.add(labelPos,
                    PVector.v(0, taggedLabels.isEmpty() ? 0 : g.textHeight()));
            PVector topListPos = PVector.add(topBarPos, PVector.v(0,
                    skipCount > 0 ? LABEL_BAR_HEIGHT + SPACING : 0));
            PVector bottomBarPos = null;

            final double barIncrement = Math.min(
                    2 * LABEL_MARKER_RADIUS + 2,
                    dim.x / (double) remainderLabels.size()
            );

            int i;

            // Place in top bar.
            for (i = 0; i < skipCount && i < remainderLabels.size(); i++) {
                SetLabel label = remainderLabels.get(i);
                label.opened = false;
                label.topLeft(PVector.v(topBarPos.x + LABEL_MARKER_RADIUS + i * barIncrement,
                        topBarPos.y + LABEL_MARKER_RADIUS));
            }

            // Place in mid section, as full.
            labelPos = topListPos;
            for (i = skipCount; i < remainderLabels.size(); i++) {
                SetLabel label = remainderLabels.get(i);
                PVector labelDim = label.dimensions(g);

                label.opened =
                        topLeft.y + labelPos.y + 2 * labelDim.y +
                                LABEL_BAR_HEIGHT + SPACING < g.getCanvasHeight();

                if (label.opened) {
                    label.topLeft(labelPos);
                }
                // Place in bottom bar.
                else {
                    if (bottomBarPos == null) {
                        bottomBarPos = labelPos;
                    }

                    label.topLeft(PVector.v(
                            topBarPos.x + LABEL_MARKER_RADIUS + i * barIncrement,
                            bottomBarPos.y + 2 * LABEL_MARKER_RADIUS));
                }

                labelPos = PVector.add(labelPos, PVector.v(0, labelDim.y + 2));
            }

            g.snippets(remainderLabels);
        }
    }

    public boolean isOpened() {
        return model.openedCategories.get().contains(element);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (isOpened()) {
            model.openedCategories.remove(element);
        } else {
            model.openedCategories.add(element);
        }
    }

    @Override
    public void mouseWheel(int rotation) {
        positionScroll = Math.max(0, Math.min(labels.size() - 1, positionScroll + rotation));
    }
}

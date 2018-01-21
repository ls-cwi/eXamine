package org.cytoscape.examine.internal.visualization;

import org.cytoscape.examine.internal.data.DataSet;
import org.cytoscape.examine.internal.data.HCategory;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.graphics.AnimatedGraphics;
import org.cytoscape.examine.internal.graphics.PVector;
import org.cytoscape.examine.internal.graphics.draw.Layout;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import org.cytoscape.examine.internal.model.Model;
import org.cytoscape.examine.internal.signal.Observer;
import org.cytoscape.examine.internal.visualization.overview.Overview;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cytoscape.examine.internal.visualization.Constants.MARGIN;

abstract class Visualization {

    private final DataSet dataSet;
    private final Model model;

    public final SetColors setColors;
    final Overview overview;
    private final List<SetList> setLists;

    Visualization(DataSet dataSet, Model model) {
        this.dataSet = dataSet;
        this.model = model;

        setColors = new SetColors(model);

        // Protein set listing, update on selection change.
        setLists = new ArrayList<SetList>();
        Observer proteinSetListObserver = new Observer() {
            public void signal() {
                setLists.clear();
            }
        };
        dataSet.categories.change.subscribe(proteinSetListObserver);

        // Overview at bottom, dominant.
        overview = new Overview(model, setColors);
    }

    void drawVisualization(AnimatedGraphics g, double availableWidth, double availableHeight) {

        // Construct set lists.
        if (setLists.isEmpty()) {
            for (HCategory d : dataSet.categories.get().values()) {
                List<SetLabel> labels = new ArrayList<SetLabel>();

                for (HSet t : d.members.subList(0, Math.min(d.maxSize, d.members.size()))) {
                    String text = t.toString();
                    labels.add(new SetLabel(dataSet, model, setColors, t, text));
                }

                setLists.add(new SetList(model, d, labels));
            }
        }

        // Enforce side margins.
        g.translate(MARGIN, MARGIN);

        // Black fill.
        g.color(Color.BLACK);

        // Normal face.
        g.textFont(org.cytoscape.examine.internal.graphics.draw.Constants.FONT);

        // Downward shifting position.
        PVector shiftPos = PVector.v();

        // Left side option snippets (includes set lists).
        List<Representation> sideSnippets = new ArrayList<Representation>();

        List<SetList> openSl = new ArrayList<SetList>();
        List<SetList> closedSl = new ArrayList<SetList>();
        for (SetList sl : setLists) {
            (model.openedCategories.get().contains(sl.element) ? openSl : closedSl)
                    .add(sl);
        }
        sideSnippets.addAll(openSl);
        sideSnippets.addAll(closedSl);

        // In case no height is available (for example, for export); act as though there is plenty.
        double availableCategoryHeight = availableHeight > 0 ? availableHeight : Double.POSITIVE_INFINITY;

        Layout.placeBelowLeftToRight(g, shiftPos, sideSnippets, MARGIN, availableCategoryHeight);
        PVector termBounds = Layout.bounds(g, sideSnippets);

        shiftPos.x += termBounds.x + MARGIN;

        // Draw protein overview.
        overview.bounds = PVector.v(availableWidth - shiftPos.x - 2 * MARGIN, availableHeight);
        overview.topLeft(shiftPos);
        g.snippet(overview);

        // Occlude any overview overspill for side lists.
        g.color(Color.WHITE);
        g.fillRect(-MARGIN, -MARGIN, shiftPos.x + MARGIN, availableHeight);
        g.snippets(sideSnippets);

        try {
            Thread.sleep(25);
        } catch (InterruptedException ex) {
            Logger.getLogger(InteractiveVisualization.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

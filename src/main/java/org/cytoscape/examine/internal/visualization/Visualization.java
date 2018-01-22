package org.cytoscape.examine.internal.visualization;

import org.cytoscape.examine.internal.Constants;
import org.cytoscape.examine.internal.data.DataSet;
import org.cytoscape.examine.internal.data.HCategory;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.graphics.AnimatedGraphics;
import org.cytoscape.examine.internal.graphics.Application;
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

// Visualization module.
public class Visualization extends Application {

    private final DataSet dataSet;
    private final Model model;

    public final boolean showScore;
    public final SetColors setColors;             // Protein set coloring.
    private final List<SetList> setLists;         // GO Term lists (per domain).
    private final Overview overview;              // Protein SOM overview.

    public Visualization(DataSet dataSet, Model model, boolean showScore) {
        this.dataSet = dataSet;
        this.model = model;
        this.showScore = showScore;

        setDefaultCloseOperation(Application.DISPOSE_ON_CLOSE);
        setTitle(Constants.APP_NAME);

        // Parameters are now set up => connect model listeners.
        model.initListeners();

        setColors = new SetColors(model);

        // Protein set listing, update on selection change.
        setLists = new ArrayList<SetList>();
        Observer proteinSetListObserver = new Observer() {
            public void signal() {
                setLists.clear();
            }
        };
        //model.orderedCategories.change.subscribe(proteinSetListObserver);
        dataSet.categories.change.subscribe(proteinSetListObserver);

        // Overview at bottom, dominant.
        overview = new Overview(model, setColors);

        // Always reset navigation state.
        model.highlightedSets.clear();
        model.highlightedProteins.clear();
        model.selection.clear();
    }

    // Processing rootDraw.
    @Override
    public void draw(AnimatedGraphics g) {
        // Construct set lists.
        if (setLists.isEmpty()) {
            for (HCategory d : dataSet.categories.get().values()) {
                List<SetLabel> labels = new ArrayList<SetLabel>();

                for (HSet t : d.members.subList(0, Math.min(d.maxSize, d.members.size()))) {
                    String text = t.toString();
                    labels.add(new SetLabel(dataSet, model, setColors, t, text, showScore));
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

        Layout.placeBelowLeftToRight(g, shiftPos, sideSnippets, MARGIN, g.applicationHeight());
        PVector termBounds = Layout.bounds(g, sideSnippets);

        shiftPos.x += termBounds.x + MARGIN;

        // Draw protein overview.
        overview.bounds = PVector.v(g.applicationWidth() - shiftPos.x - 2 * MARGIN, g.applicationHeight());
        overview.topLeft(shiftPos);
        g.snippet(overview);

        // Occlude any overview overspill for side lists.
        g.color(Color.WHITE);
        g.fillRect(-MARGIN, -MARGIN, shiftPos.x + MARGIN, g.applicationHeight());
        g.snippets(sideSnippets);

        try {
            Thread.sleep(25);
        } catch (InterruptedException ex) {
            Logger.getLogger(Visualization.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void dispose() {
        overview.stop();    // Request overview animation stop.
        super.dispose();
    }

}

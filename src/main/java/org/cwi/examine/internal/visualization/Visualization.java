package org.cwi.examine.internal.visualization;

import java.awt.Color;

import org.cwi.examine.internal.graphics.Application;
import org.cwi.examine.internal.graphics.draw.Layout;
import org.cwi.examine.internal.graphics.draw.Representation;
import org.cwi.examine.internal.model.Model;

import org.cwi.examine.internal.data.HCategory;
import org.cwi.examine.internal.data.HAnnotation;
import org.cwi.examine.internal.visualization.overview.Overview;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cwi.examine.internal.graphics.PVector;

import javax.swing.*;

import static org.cwi.examine.internal.graphics.StaticGraphics.*;
import static org.cwi.examine.internal.visualization.Parameters.*;

// Visualization module.
public class Visualization extends Application {

    public static final String APPLICATION_TITLE = "eXamol";

    public final Model model;
    private List<SetList> setLists;  // GO Term lists (per domain).
    private Overview overview;       // Protein SOM overview.

    public SetColors setColors;      // Protein set coloring.

    public Visualization(final Model model) {
        this.model = model;

        setColors = new SetColors(model.selection);

        setTitle(APPLICATION_TITLE);

        // Protein set listing, update on selection change.
        setLists = new ArrayList<>();
        model.activeNetwork.addListener((observable, old, activeNetwork) -> setLists.clear());

        // Overview at bottom, dominant.
        overview = new Overview(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    // Processing rootDraw.
    @Override
    public void draw() {
        // Catch thread exceptions (this is nasty, TODO: pretty fix).
        try {
            // Construct set lists.
            if(setLists.isEmpty()) {
                for(HCategory d: model.activeNetwork.get().categories) {
                    List<SetLabel> labels = new ArrayList<>();

                    for(HAnnotation t: d.annotations) {
                        String text = t.toString();
                        labels.add(new SetLabel(this, t, text));
                    }

                    setLists.add(new SetList(this, d, labels));
                }
            }
            
            // Enforce side margins.
            translate(MARGIN, MARGIN);

            // Black fill.
            color(Color.BLACK);

            // Normal face.
            textFont(org.cwi.examine.internal.graphics.draw.Parameters.font);

            // Downward shifting position.
            PVector shiftPos = PVector.v();

            // Left side option snippets (includes set lists).
            List<Representation> sideSnippets = new ArrayList<>();
            
            List<SetList> openSl = new ArrayList<>();
            List<SetList> closedSl = new ArrayList<>();
            for(SetList sl: setLists) {
                (model.openedCategories.contains(sl.element) ? openSl : closedSl)
                .add(sl);
            }
            sideSnippets.addAll(openSl);
            sideSnippets.addAll(closedSl);

            Layout.placeBelowLeftToRight(shiftPos, sideSnippets, MARGIN, sceneHeight());
            PVector termBounds = Layout.bounds(sideSnippets);

            shiftPos.x += termBounds.x + MARGIN;
        
            // Draw protein overview.
            overview.bounds = PVector.v(sceneWidth() - shiftPos.x - 2 * MARGIN, sceneHeight());
            overview.topLeft(shiftPos);
            snippet(overview);
            
            // Occlude any overview overspil for side lists.
            color(Color.WHITE);
            fillRect(-MARGIN, -MARGIN, shiftPos.x + MARGIN, sketchHeight());
            snippets(sideSnippets);

            try {
                Thread.sleep(10);
            } catch(InterruptedException ex) {
                Logger.getLogger(Visualization.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch(Exception ex) {
            
        }
    }

    // Terminate overview on disposal.
    @Override
    public void dispose() {
        overview.stop();
        super.dispose();
    }
}

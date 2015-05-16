package org.cytoscape.examine.internal.visualization;

import java.awt.Color;
import java.awt.event.KeyEvent;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import org.cytoscape.examine.internal.graphics.Application;
import org.cytoscape.examine.internal.graphics.draw.Layout;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import org.cytoscape.examine.internal.signal.Observer;
import static org.cytoscape.examine.internal.Modules.*;
import static org.cytoscape.examine.internal.visualization.Parameters.*;

import org.cytoscape.examine.internal.Constants;
import org.cytoscape.examine.internal.data.HCategory;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.visualization.overview.Overview;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.cytoscape.examine.internal.graphics.PVector;

// Visualization module.
public class Visualization extends Application {
    public SetColors setColors;             // Protein set coloring.
    private List<SetList> setLists;  // GO Term lists (per domain).
    private Overview overview;              // Protein SOM overview.
    
    @Override
    public void initialize() {
        this.setTitle(Constants.APP_NAME);
        
        // Parameters are now set up => connect model listeners.
        model.initListeners();
        
        setColors = new SetColors();
        
        // Protein set listing, update on selection change.
        setLists = new ArrayList<SetList>();
        Observer proteinSetListObserver = new Observer() {
            public void signal() {
                setLists.clear();
            }
        };
        //model.orderedCategories.change.subscribe(proteinSetListObserver);
        data.categories.change.subscribe(proteinSetListObserver);
        
        // Overview at bottom, dominant.
        overview = new Overview();
        
        // Always reset navigation state.
        model.highlightedSets.clear();
        model.highlightedProteins.clear();
        model.selection.clear();
    }
    
    // Processing rootDraw.
    @Override
    public void draw() {
        // Catch thread exceptions (this is nasty, TODO: pretty fix).
        try {
            // Construct set lists.
            if(setLists.isEmpty()) {
                for(HCategory d: data.categories.get().values()) {
                    List<SetLabel> labels = new ArrayList<SetLabel>();

                    for(HSet t: d.members.subList(0, Math.min(d.maxSize, d.members.size()))) {
                        String text = t.toString();
                        labels.add(new SetLabel(t, text));
                    }

                    setLists.add(new SetList(d, labels));
                }
            }
            
            // Enforce side margins.
            translate(MARGIN, MARGIN);

            // Black fill.
            color(Color.BLACK);

            // Normal face.
            textFont(org.cytoscape.examine.internal.graphics.draw.Parameters.font);

            // Downward shifting position.
            PVector shiftPos = PVector.v();

            // Left side option snippets (includes set lists).
            List<Representation> sideSnippets = new ArrayList<Representation>();
            
            List<SetList> openSl = new ArrayList<SetList>();
            List<SetList> closedSl = new ArrayList<SetList>();
            for(SetList sl: setLists) {
                (model.openedCategories.get().contains(sl.element) ? openSl : closedSl)
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

    /*@Override
    public void mouseClicked(MouseEvent e) {
        Object hovered = StaticGraphics.hovered();
        
        HElement element = hovered == null || !(hovered instanceof HElement) ?
                                null :
                                (HElement) hovered;
        
        // Clear selection.
        model.selection.select(element);
    }*/

    // Terminate overview on disposal.
    @Override
    public void dispose() {
        overview.stop();
        super.dispose();
    }
}

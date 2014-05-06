package org.cytoscape.examine.internal.visualization;

import java.awt.Color;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import org.cytoscape.examine.internal.graphics.Application;
import static org.cytoscape.examine.internal.graphics.Math.*;
import org.cytoscape.examine.internal.graphics.draw.Layout;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import org.cytoscape.examine.internal.signal.Observer;
import static org.cytoscape.examine.internal.Modules.*;
import static org.cytoscape.examine.internal.visualization.Parameters.*;

import org.cytoscape.examine.internal.Constants;
import org.cytoscape.examine.internal.data.HCategory;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.visualization.overview.SOMOverview;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ToolTipManager;
import org.cytoscape.examine.internal.graphics.PVector;

/**
 * Visualization module.
 */
public class Visualization extends Application {
    
    // Protein set coloring.
    public SetColors setColors;
    
    // GO Term lists (per domain).
    private List<SetList> proteinSetLists;
    
    // Protein SOM overview.
    private SOMOverview overview;

    /**
     * Base constructor.
     */
    @Override
    public void initialize() {
        this.setTitle(Constants.APP_NAME);
        
        // Parameters are now set up => connect model listeners.
        model.initListeners();
        
        setColors = new SetColors();
        
        // Protein set listing, update on selection change.
        proteinSetLists = new ArrayList<SetList>();
        
        Observer proteinSetListObserver = new Observer() {

            public void signal() {
                updateProteinSetLists();
            }
            
        };
        //model.orderedCategories.change.subscribe(proteinSetListObserver);
        data.categories.change.subscribe(proteinSetListObserver);
        updateProteinSetLists();
        
        // Overview at bottom, dominant.
        overview = new SOMOverview();
        
        // Always reset navigation state.
        model.highlightedSets.clear();
        model.highlightedProteins.clear();
        model.selection.clear();
    }
    
    /**
     * Processing rootDraw.
     */
    @Override
    public void draw() {
        // Catch thread exceptions (this is nasty, TODO: pretty fix).
        try {        
            // Enforce side margins.
            translate(margin.get(), margin.get());

            // Black fill.
            color(Color.BLACK);

            // Normal face.
            textFont(org.cytoscape.examine.internal.graphics.draw.Parameters.font.get());

            // Downward shifting position.
            PVector shiftPos = v();

            // Left side option snippets (includes protein set lists).
            List<Representation> sideSnippets = new ArrayList<Representation>();
            
            List<SetList> openSl = new ArrayList<SetList>();
            List<SetList> closedSl = new ArrayList<SetList>();
            for(SetList sl: proteinSetLists) {
                (model.openedCategories.get().contains(sl.element) ? openSl : closedSl)
                .add(sl);
            }
            sideSnippets.addAll(openSl);
            sideSnippets.addAll(closedSl);

            Layout.placeBelowLeftToRight(shiftPos, sideSnippets, margin.get(), sceneHeight());
            PVector termBounds = Layout.bounds(sideSnippets);
            snippets(sideSnippets);

            shiftPos.x += termBounds.x + margin.get();
        
            // Draw protein overview.
            overview.bounds = v(sceneWidth() - shiftPos.x - 2 * margin.get(),
                                sceneHeight());
            overview.topLeft(shiftPos);
            snippet(overview);

            try {
                Thread.sleep(10);
            } catch(InterruptedException ex) {
                Logger.getLogger(Visualization.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch(Exception ex) {
            
        }
    }
    
    /**
     * Construct new protein set lists.
     */
    private void updateProteinSetLists() {
        proteinSetLists.clear();
        
        // Set categories.
        for(HCategory d: data.categories.get().values()) {
            List<SetLabel> labels = new ArrayList<SetLabel>();
            
            for(HSet t: d.members.subList(0, Math.min(d.maxSize, d.members.size()))) {
                String text = t.toString();
                labels.add(new SetLabel(t, text));
            }
            
            proteinSetLists.add(new SetList(d, labels));
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

    /**
     * Terminate overview on disposal.
     */
    @Override
    public void dispose() {
        overview.stop();
        
        super.dispose();
    }
    
}

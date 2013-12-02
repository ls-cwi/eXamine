package org.cytoscape.examine.internal.visualization;

import aether.Aether;
import static aether.Aether.*;
import aether.Application;
import static aether.Math.*;
import aether.draw.Layout;
import aether.draw.Representation;
import aether.signal.Observer;
import static org.cytoscape.examine.internal.Modules.*;
import static org.cytoscape.examine.internal.visualization.Parameters.*;

import org.cytoscape.examine.internal.Constants;
import org.cytoscape.examine.internal.data.HCategory;
import org.cytoscape.examine.internal.data.HElement;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.visualization.overview.SOMOverview;

import processing.core.PVector;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static processing.core.PConstants.*;

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
     * Base constructor (for Aether application).
     */
    @Override
    public void initialize() {
        // Parameters are now set up => connect model listeners.
        model.initListeners();
        
        // Default size
        setSize(1014, 768);
        
        setColors = new SetColors();
        
        this.setTitle(Constants.APP_NAME);
        
        // Protein set listing, update on selection change.
        proteinSetLists = new ArrayList<SetList>();
        
        Observer proteinSetListObserver = new Observer() {

            public void signal() {
                updateProteinSetLists();
            }
            
        };
        data.categories.change.subscribe(proteinSetListObserver);
        //Parameters.visualSetsPerCategory.change.subscribe(proteinSetListObserver);
        updateProteinSetLists();
        
        //((JFrame) this.getParent()).setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        /*model.selection.change.subscribe(new Observer() {

            @Override
            public void signal() {
                updateProteinSetLists();
            }
            
        });
        model.selection.change.signal();*/
        
        // Overview at bottom, dominant.
        overview = new SOMOverview();
        
        // Always reset navigation state.
        model.highlightedSets.clear();
        model.highlightedProteins.clear();
        model.selection.clear();
        
        System.out.println("Initialize visualization module.");
    }
    
    /**
     * Processing draw.
     */
    @Override
    public void draw() {
        // Catch thread exceptions (this is nasty, TODO: pretty fix).
        try {        
            // Enforce side margins.
            translate(margin.get(), margin.get());

            // Black fill.
            fill(0f);

            // Round cap and join.
            strokeCap(PROJECT);
            strokeJoin(ROUND);

            // Normal face.
            textFont(aether.draw.Parameters.font.get());

            // Downward shifting position.
            PVector shiftPos = v();

            // Left side option snippets (includes protein set lists).
            List<Representation> sideSnippets = new ArrayList<Representation>();
            sideSnippets.addAll(proteinSetLists);

            Layout.placeBelowLeftToRight(shiftPos, sideSnippets, margin.get(), sceneHeight());
            PVector termBounds = Layout.bounds(sideSnippets);
            snippets(sideSnippets);

            shiftPos.x += termBounds.x + margin.get();
        
            // Draw protein overview.
            overview.bounds = v(sceneWidth() - shiftPos.x - 2f * margin.get(),
                                sceneHeight()); //Math.min(sceneHeight(), termBounds.y));
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
        
        System.out.println("Update set lists.");
        
        // GO terms.
        for(HCategory d: data.categories.get().values() ) {
            List<SetLabel> labels = new ArrayList<SetLabel>();
            
            System.out.println("Construct: " + d.name);
            
            for(HSet t: d.members.subList(0, Math.min(d.maxSize, d.members.size()))) {
                String text = t.toString();
                labels.add(new SetLabel(t, text));
            }
            
            proteinSetLists.add(new SetList(d, labels));
        }
    }

    @Override
    public void mouseClicked() {
        Object hovered = Aether.hovered();
        
        HElement element = hovered == null || !(hovered instanceof HElement) ?
                                null :
                                (HElement) hovered;
        
        // Clear selection.
        model.selection.select(element);
    }

    /**
     * Terminate overview on disposal.
     */
    @Override
    public void dispose() {
        overview.stop();
        
        super.dispose();
    }
    
}

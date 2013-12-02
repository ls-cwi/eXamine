package org.cytoscape.examine.internal.visualization;

import static aether.color.Color.*;
import aether.signal.Observer;
import static org.cytoscape.examine.internal.Modules.*;

import org.cytoscape.examine.internal.data.HSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import processing.core.PVector;

/**
 *
 */
public class SetColors implements Observer {
    
    // Predefined protein set to color mapping.
    private final Map<HSet, PVector> predefinedColorMap;
    
    // Dynamic protein set to color mapping.
    private final Map<HSet, PVector> colorMap;
    
    // Available set colors.
    private final ArrayList<PVector> availableColors;
    
    // Source color pool.
    public static final PVector[] palette = new PVector[] {
            rgb(141, 211, 199),
            rgb(255, 255, 179),
            rgb(190, 186, 218),
            rgb(251, 128, 114),
            rgb(128, 177, 211),
            rgb(253, 180, 98),
            //rgb(179, 222, 105),
            rgb(252, 205, 229),
            rgb(217, 217, 217),
            rgb(188, 128, 189),
            rgb(204, 235, 197),
            rgb(255, 237, 111)           
    };

    /**
     * Base constructor.
     */
    public SetColors() {
        colorMap = new HashMap<HSet, PVector>();
        availableColors = new ArrayList<PVector>();
        availableColors.addAll(Arrays.asList(SetColors.palette));
        
        // Predefined colors for expression sets (log FC and score derived).
        predefinedColorMap = new HashMap<HSet, PVector>();
        
        // Listen to model and parameter changes.
        model.selection.change.subscribe(this);
        
        signal();
    }
    
    @Override
    public void signal() {
        Set<HSet> newActiveSets = new HashSet<HSet>();
        newActiveSets.addAll(model.selection.activeSetList);
        newActiveSets.removeAll(colorMap.keySet());
        newActiveSets.removeAll(predefinedColorMap.keySet());
        
        Set<HSet> newDormantSets = new HashSet<HSet>();
        newDormantSets.addAll(colorMap.keySet());
        newDormantSets.removeAll(model.selection.activeSetList);
        newDormantSets.removeAll(predefinedColorMap.keySet());
        
        // Assign colors to new active sets.
        for(HSet pS: newActiveSets) {
            colorMap.put(pS, availableColors.remove(0));
        }
        
        // Release colors of new dormant sets.
        for(HSet pS: newDormantSets) {
            availableColors.add(colorMap.remove(pS));
        }
    }
    
    /**
     * Get the color that has been assigned to the given set.
     */
    public PVector color(HSet proteinSet) {
        PVector result = predefinedColorMap.get(proteinSet);
        
        if(result == null) {
            result = colorMap.get(proteinSet);
        }
        
        return result;
    }
    
}

package org.cytoscape.examine.internal.visualization;

import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.model.Model;
import org.cytoscape.examine.internal.signal.Observer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Selected set to color map.
public class SetColors implements Observer {

    // Source color pool.
    public static final Color[] PALETTE = new Color[] {
            new Color(141, 211, 199),
            new Color(255, 255, 179),
            new Color(190, 186, 218),
            new Color(251, 128, 114),
            new Color(128, 177, 211),
            new Color(253, 180, 98),
            new Color(252, 205, 229),
            new Color(188, 128, 189),
            new Color(204, 235, 197),
            new Color(255, 237, 111)
    };

    private final Model model;

    private final Map<HSet, Color> predefinedColorMap;  // Predefined protein set to color mapping.
    private final Map<HSet, Color> colorMap;            // Dynamic protein set to color mapping.
    private final ArrayList<Color> availableColors;     // Available set colors.
    
    public SetColors(Model model) {
        this.model = model;

        colorMap = new HashMap<HSet, Color>();
        availableColors = new ArrayList<Color>();
        availableColors.addAll(Arrays.asList(SetColors.PALETTE));
        
        // Predefined colors for expression sets (log FC and score derived).
        predefinedColorMap = new HashMap<HSet, Color>();
        
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
    
    // Get the color that has been assigned to the given set.
    public Color color(HSet proteinSet) {
        Color result = predefinedColorMap.get(proteinSet);
        
        if(result == null) {
            result = colorMap.get(proteinSet);
        }
        
        return result;
    }
}

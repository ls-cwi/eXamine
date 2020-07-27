package org.cwi.examine.internal.visualization;

import java.awt.Color;

import javafx.collections.ListChangeListener;
import org.cwi.examine.internal.model.Selection;

import org.cwi.examine.internal.data.HAnnotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Selected set to color map.
public class SetColors implements ListChangeListener<HAnnotation> {
    private final Selection selection;
    private final Map<HAnnotation, Color> predefinedColorMap;  // Predefined protein set to color mapping.
    private final Map<HAnnotation, Color> colorMap;            // Dynamic protein set to color mapping.
    private final ArrayList<Color> availableColors;            // Available set colors.
    
    // Source color pool.
    public static final Color[] palette = new Color[] {
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
    
    public SetColors(final Selection selection) {
        this.selection = selection;

        colorMap = new HashMap<>();
        availableColors = new ArrayList<>();
        availableColors.addAll(Arrays.asList(SetColors.palette));
        
        // Predefined colors for expression sets (log FC and score derived).
        predefinedColorMap = new HashMap<>();
        
        // Listen to model and parameter changes.
        selection.activeSetList.addListener(this);
    }

    public void onChanged(ListChangeListener.Change<? extends HAnnotation> change) {
        Set<HAnnotation> newActiveSets = new HashSet<>();
        newActiveSets.addAll(selection.activeSetList);
        newActiveSets.removeAll(colorMap.keySet());
        newActiveSets.removeAll(predefinedColorMap.keySet());
        
        Set<HAnnotation> newDormantSets = new HashSet<>();
        newDormantSets.addAll(colorMap.keySet());
        newDormantSets.removeAll(selection.activeSetList);
        newDormantSets.removeAll(predefinedColorMap.keySet());
        
        // Assign colors to new active sets.
        for(HAnnotation pS: newActiveSets) {
            colorMap.put(pS, availableColors.remove(0));
        }
        
        // Release colors of new dormant sets.
        for(HAnnotation pS: newDormantSets) {
            availableColors.add(colorMap.remove(pS));
        }
    }
    
    // Get the color that has been assigned to the given set.
    public Color color(HAnnotation proteinSet) {
        Color result = predefinedColorMap.get(proteinSet);
        
        if(result == null) {
            result = colorMap.get(proteinSet);
        }
        
        return result;
    }
}

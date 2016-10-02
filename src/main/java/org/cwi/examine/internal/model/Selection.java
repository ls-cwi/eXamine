package org.cwi.examine.internal.model;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.collections.ObservableMapWrapper;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.cwi.examine.internal.data.HAnnotation;
import org.cwi.examine.internal.data.HElement;
import org.cwi.examine.internal.visualization.SetColors;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The sets that are to be displayed, including additional
 * information about importance.
 */
public final class Selection {
    
    // Included sets and the weight that has been assigned to them.
    public final ObservableMap<HAnnotation, Double> activeSetMap;
    
    // List of active sets with a somewhat stable ordering.
    public final ObservableList<HAnnotation> activeSetList;

    // Selected set or protein.
    public HElement selected;
    
    public Selection() {
        this.activeSetMap = new ObservableMapWrapper<>(new HashMap<>());
        this.activeSetList = new ObservableListWrapper<>(new ArrayList<>());
        this.selected = null;
    }
    
    /**
     * Add set with an initial weight, report on success
     * (there is a maximum number of selected sets).
     */
    public boolean add(HAnnotation proteinSet, double weight) {
        boolean added = activeSetList.size() < SetColors.palette.length;
        
        if(added) {
            activeSetMap.put(proteinSet, weight);
            activeSetList.add(proteinSet);
        }
        
        return added;
    }
    
    /**
     * Remove set.
     */
    public void remove(HAnnotation proteinSet) {
        activeSetMap.remove(proteinSet);
        activeSetList.remove(proteinSet);
    }
    
    /**
     * Select a set or protein, null iff no element is selected.
     */
    public void select(HElement element) {
        selected = element;
        
        // Element is a set -> remove from or include in active sets.
        if(element != null && element instanceof HAnnotation) {
            HAnnotation elSet = (HAnnotation) element;
            
            if(activeSetList.contains(elSet)) {
                remove(elSet);
            } else {
                add(elSet, 1);
            }
        }
    }
    
    /**
     * Adjust the weight of a set by the given change.
     */
    public void changeWeight(HAnnotation proteinSet, double weightChange) {
        double currentWeight = activeSetMap.get(proteinSet);
        double newWeight = Math.max(1f, currentWeight + weightChange);
        activeSetMap.put(proteinSet, newWeight);
    }
}

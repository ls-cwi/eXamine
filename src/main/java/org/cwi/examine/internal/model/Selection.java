package org.cwi.examine.internal.model;

import org.cwi.examine.internal.data.HElement;
import org.cwi.examine.internal.data.HNode;
import org.cwi.examine.internal.signal.Subject;
import org.cwi.examine.internal.visualization.SetColors;
import org.cwi.examine.internal.data.HAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The sets that are to be displayed, including additional
 * information about importance.
 */
public final class Selection {
    
    // Change signal.
    public final Subject change;
    
    // Included sets and the weight that has been assigned to them.
    public final Map<HAnnotation, Double> activeSetMap;
    
    // List of active sets with a somewhat stable ordering.
    public final List<HAnnotation> activeSetList;
    
    // Selected set or protein.
    public HElement selected;
    
    public Selection(final Model model) {
        this.change = new Subject();
        this.activeSetMap = new HashMap<HAnnotation, Double>();
        this.activeSetList = new ArrayList<HAnnotation>();
        this.selected = null;
    }
    
    /**
     * Clear all selection information.
     */
    public void clear() {
        this.activeSetMap.clear();
        this.activeSetList.clear();
        this.activeSetList.clear();
        this.selected = null;
        
        this.change.signal();
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
            
            change.signal();
        }
        
        return added;
    }
    
    /**
     * Remove set.
     */
    public void remove(HAnnotation proteinSet) {
        activeSetMap.remove(proteinSet);
        activeSetList.remove(proteinSet);
        
        change.signal();
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
        } else {
            change.signal();
        }
    }
    
    /**
     * Adjust the weight of a set by the given change.
     */
    public void changeWeight(HAnnotation proteinSet, double weightChange) {
        double currentWeight = activeSetMap.get(proteinSet);
        double newWeight = Math.max(1f, currentWeight + weightChange);
        activeSetMap.put(proteinSet, newWeight);
        
        change.signal();
    }
    
    // Change the contents of a protein set (and propagate change signal).
    public void update(HAnnotation newProteinSet) {
        // Remove old proteinset with identity of new one.
        if(activeSetMap.containsKey(newProteinSet)) {
            double weight = activeSetMap.remove(newProteinSet);
            activeSetMap.put(newProteinSet, weight);
            activeSetList.set(activeSetList.indexOf(newProteinSet), newProteinSet);
        } else {
            activeSetMap.put(newProteinSet, 1.0);
        }
        
        change.signal();
    }
    
    /**
     * Get the nodes that are selected (either a single selected node,
     * or the nodes of a selected set).
     */
    public Set<HNode> selectedNodes(boolean intersection) {
        Set<HNode> result = new HashSet<HNode>();
        
        if (intersection) {
            if (activeSetList.size() > 0) {
                    result.addAll(activeSetList.get(0).elements);
                    for(HAnnotation s: activeSetList) {
                        result.retainAll(s.elements);
                    }
            }
        } else {
            for(HAnnotation s: activeSetList) {
                result.addAll(s.elements);
            }
        }
        
        return result;
    }
}

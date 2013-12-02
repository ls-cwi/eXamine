package org.cytoscape.examine.internal.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.cytoscape.group.CyGroup;

/**
 * Category of HSets.
 */
public class HCategory {
    
    // Wrapped CyGroup.
    private final CyGroup cyGroup;
    
    // Name.
    public final String name;
    
    // Member sets.
    public final List<HSet> members;
    
    // Maximal size.
    public final int maxSize;
    
    /**
     * Base constructor.
     */
    public HCategory(CyGroup cyGroup, String name, List<HSet> members, int maxSize) {
        this.cyGroup = cyGroup;
        this.name = name;
        this.members = members;
        this.maxSize = maxSize;
        
        // Sort sets by score, then alphabet.
        Collections.sort(members, new Comparator<HSet> () {

            public int compare(HSet lS, HSet rS) {
                int result;
                
                if(lS.score == rS.score) {
                    result = lS.name.compareTo(rS.name);
                } else {
                    result = Float.isNaN(lS.score) || lS.score > rS.score ? 1 : -1;
                }
                
                return result;
            }
    
        });
    }

    @Override
    public String toString() {
        return name;
    }
    
}

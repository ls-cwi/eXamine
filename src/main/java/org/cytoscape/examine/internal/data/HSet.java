package org.cytoscape.examine.internal.data;

import java.util.HashSet;
import java.util.List;

import java.util.Set;
import org.cytoscape.group.CyGroup;

/**
 * An set of nodes.
 */
public class HSet extends HElement {
    
    // Wrapped CyGroup.
    public final CyGroup cyGroup;
    
    // Optional score (lower is better -> p-value).
    public final double score;
    
    // Wrapped set.
    public final List<HNode> elements;
    public final Set<HNode> set;
    
    /**
     * Base constructor.
     */
    public HSet(CyGroup cyGroup, String name, double score, String url, List<HNode> members) {
        super(name, url);
        
        this.cyGroup = cyGroup;
        this.score = score; // Double.isNaN(score) || score > 0.9 ? 0.9 : score; // Default score to 1.
        this.elements = members;
        this.set = new HashSet<HNode>(members);
    }
    
    /**
     * Textual representation
     */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + this.name.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final HSet other = (HSet) obj;
        if(!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
    
}

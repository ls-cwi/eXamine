package org.cwi.examine.internal.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Set;

/**
 * An set of nodes.
 */
public class HAnnotation extends HElement {

    public final double score;                              // Optional score (lower is better -> p-value).
    public final List<HNode> elements = new ArrayList<>();  // Wrapped set.
    public final Set<HNode> set = new HashSet<>();
    
    /**
     * Base constructor.
     */
    public HAnnotation(final String identifier, final String name, final double score, final String url) {
        super(identifier, name, url);
        this.score = score; // Double.isNaN(score) || score > 0.9 ? 0.9 : score; // Default score to 1.
    }

    protected void addMember(HNode node) {
        elements.add(node);
        set.add(node);
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
        final HAnnotation other = (HAnnotation) obj;
        if(!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
    
}

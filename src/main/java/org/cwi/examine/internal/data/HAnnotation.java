package org.cwi.examine.internal.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Set;

/**
 * An set of nodes.
 */
public class HAnnotation extends HElement {

    public final List<HNode> elements = new ArrayList<>();
    public final Set<HNode> set = new HashSet<>();

    public HAnnotation(final String identifier, final String name, final String url, final double score) {
        super(identifier, name, url, score);
    }

    protected void addMember(HNode node) {
        elements.add(node);
        set.add(node);
    }

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

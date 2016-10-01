package org.cwi.examine.internal.data;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a node of interest.
 */
public class HNode extends HElement {

    public final Set<HAnnotation> annotations;

    public HNode(final String id, final String name, final String url, final double score) {
        super(id, name, url, score);
        this.annotations = new HashSet<>();
    }

    protected void addAnnotation(HAnnotation annotation) {
        this.annotations.add(annotation);
    }

    @Override
    public String toString() {
        return name;
    }
}

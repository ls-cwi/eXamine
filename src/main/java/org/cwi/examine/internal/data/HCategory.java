package org.cwi.examine.internal.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Category of annotations.
 */
public class HCategory {

    public static int MAXIMUM_SIZE = 50;

    public final String name;
    public final List<HAnnotation> annotations;

    public HCategory(final String name, final List<HAnnotation> annotations) {
        this.name = name;

        // Sort annotations by score, then alphabet.
        final List<HAnnotation> topAnnotations = new ArrayList<>(annotations);
        Collections.sort(topAnnotations, (lS, rS) -> {
            int result;

            if(lS.score == rS.score) {
                result = lS.name.compareTo(rS.name);
            } else {
                result = Double.isNaN(lS.score) || lS.score > rS.score ? 1 : -1;
            }

            return result;
        });

        this.annotations = topAnnotations.subList(0, Math.min(topAnnotations.size(), MAXIMUM_SIZE));
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HCategory other = (HCategory) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
}

package org.cytoscape.examine.internal.data;

import org.cytoscape.group.CyGroup;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Category of HSets.
 */
public class HCategory {

    // Wrapped CyGroup.
    //private final CyGroup cyGroup;

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
        //this.cyGroup = cyGroup;
        this.name = name;
        this.members = members;
        this.maxSize = maxSize;

        // Sort sets by score, then alphabet.
        Collections.sort(members, new Comparator<HSet>() {

            public int compare(HSet lS, HSet rS) {
                final int result;

                final double leftScore = Double.isNaN(lS.score) ? Double.POSITIVE_INFINITY : lS.score;
                final double rightScore = Double.isNaN(rS.score) ? Double.POSITIVE_INFINITY : rS.score;

                if (leftScore == rightScore) {
                    result = lS.name.compareTo(rS.name);
                } else {
                    result = (int) Math.signum(leftScore - rightScore);
                }

                return result;
            }

        });
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

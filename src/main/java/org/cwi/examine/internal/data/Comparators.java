package org.cwi.examine.internal.data;

import java.util.Comparator;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Linear ordering comparators for various types.
 */
public class Comparators {
    
    /**
     * The unit comparator regards every element as equal.
     */
    public static <E> Comparator<E> unit() {
        return (e1, e2) -> 0;
    }
    
    /**
     * Compares strings by alphanumeric ordering.
     */
    public static final Comparator stringIgnoreCase =
            (left, right) -> left.toString().compareToIgnoreCase(right.toString());
    
    /**
     * Concatenates multiple comparators in a lexicographic manner.
     */
    public static <E> Comparator<E> lexicographic(final Comparator<E>... comparators) {
        return (left, right) -> {
            int comparison;

            int i = 0;
            do {
                comparison = comparators[i].compare(left, right);
                i++;
            } while(comparison == 0 && i < comparators.length);

            return comparison;
        };
    }
    
    /**
     * Compares networks by size.
     */
    public static Comparator<Network> networkSize() {
        return (left, right) -> left.graph.vertexSet().size() - right.graph.vertexSet().size();
    }
    
}

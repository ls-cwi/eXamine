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
        return new Comparator<E>() {

            @Override
            public int compare(E e1, E e2) {
                return 0;
            }

        };
    }
    
    /**
     * Compares strings by alphanumeric ordering.
     */
    public static final Comparator stringIgnoreCase =
        new Comparator() {

            @Override
            public int compare(Object left, Object right) {
                return left.toString().compareToIgnoreCase(right.toString());
            }

        };
    
    /**
     * Concatenates multiple comparators in a lexicographic manner.
     */
    public static <E> Comparator<E> lexicographic(final Comparator<E>... comparators) {
        return new Comparator<E>() {

            @Override
            public int compare(E left, E right) {
                int comparison;
                
                int i = 0;
                do {
                    comparison = comparators[i].compare(left, right);
                    i++;
                } while(comparison == 0 && i < comparators.length);
                
                return comparison;
            }
        
        };
    }
    
    /**
     * Compares graphs by size.
     */    
    public static <V, E> Comparator<Graph<String, DefaultEdge>> graphSize() {
        return new Comparator<Graph<String, DefaultEdge>>() {

            @Override
            public int compare(Graph<String, DefaultEdge> left,
                               Graph<String, DefaultEdge> right) {
                return left.vertexSet().size() - right.vertexSet().size();
            }

        };
    }
    
    /**
     * Compares networks by size.
     */
    public static <V, E> Comparator<Network> networkSize() {
        return new Comparator<Network>() {

            @Override
            public int compare(Network left, Network right) {
                return left.graph.vertexSet().size() -
                       right.graph.vertexSet().size();
            }

        };
    }
    
}

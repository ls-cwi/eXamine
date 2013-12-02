package org.cytoscape.examine.internal.data;

import org.cytoscape.model.CyNetwork;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Represents the entire network of interest, which
 * is a superset of all other networks.
 */
public class SuperNetwork extends Network {
    
    // Wrapped cynetwork.
    public final CyNetwork cyNetwork;

    public SuperNetwork(CyNetwork cyNetwork, UndirectedGraph<HNode, DefaultEdge> graph) {
        super(graph);
        
        this.cyNetwork = cyNetwork;
    }
    
}

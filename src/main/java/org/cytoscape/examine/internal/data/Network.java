package org.cytoscape.examine.internal.data;

import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.graph.UndirectedSubgraph;

/**
 * Network that wraps a graph with additional information.
 */
public class Network {
    
    // Wrapped graph.
    public final UndirectedGraph<HNode, DefaultEdge> graph;
    
    /**
     * Base constructor.
     */
    public Network(final UndirectedGraph<HNode, DefaultEdge> graph) {
        // Immutable graph.
        this.graph = graph;
    }
    
    /**
     * Induce subgraph from super network.
     */
    public static UndirectedGraph<HNode, DefaultEdge>
            induce(Set<HNode> nodeSubset, SuperNetwork superNetwork) {
        // Verify whether entire subset is present in super network.
        for(HNode node: nodeSubset) {
            if(!superNetwork.graph.containsVertex(node)) {
                System.err.println("Sub network node " + node +
                                   " not contained by super network.");
            }
        }
        
        Graph<HNode, DefaultEdge> subGraph =
                new Subgraph<>(superNetwork.graph, nodeSubset);
        
        return new UndirectedSubgraph<HNode,DefaultEdge>(superNetwork.graph,
                                      subGraph.vertexSet(),
                                      subGraph.edgeSet());
    }
    
}

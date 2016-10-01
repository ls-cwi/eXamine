package org.cwi.examine.internal.data;

import java.util.*;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.graph.UndirectedSubgraph;

/**
 * Network that wraps a graph with additional information.
 */
public class Network {

    public final UndirectedGraph<HNode, DefaultEdge> graph;
    public final List<HCategory> categories;
    public final List<HAnnotation> annotations;
    public final double minNodeScore, maxNodeScore;
    public final double minAnnotationScore, maxAnnotationScore;

    public Network() {
        this(new Pseudograph<>(DefaultEdge.class), new ArrayList<>());
    }

    public Network(final UndirectedGraph<HNode, DefaultEdge> graph, final List<HCategory> categories) {
        this.graph = graph;
        this.categories = categories;
        this.annotations = new ArrayList<>();
        categories.forEach(category -> annotations.addAll(category.annotations));

        Set<HNode> nodes = graph.vertexSet();
        minNodeScore = nodes.stream().map(n -> n.score).min(Double::compare).orElse(0.);
        maxNodeScore = nodes.stream().map(n -> n.score).max(Double::compare).orElse(1.);
        minAnnotationScore = annotations.stream().map(a -> a.score).min(Double::compare).orElse(0.);
        maxAnnotationScore = annotations.stream().map(a -> a.score).max(Double::compare).orElse(1.);
    }
    
    /**
     * Induce sub network from super network.
     */
    public static Network induce(final Set<HNode> nodesToInclude, final Network superNetwork) {
        // Verify whether entire subset is present in super network.
        nodesToInclude.stream()
                .filter(node -> !superNetwork.graph.containsVertex(node))
                .forEach(node -> System.err.println(
                        "Sub network node " + node + " not contained by super network."));

        final Graph<HNode, DefaultEdge> subGraph = new Subgraph(superNetwork.graph, nodesToInclude);
        final UndirectedGraph<HNode, DefaultEdge> undirectedSubGraph =
                new UndirectedSubgraph(superNetwork.graph, subGraph.vertexSet(), subGraph.edgeSet());

        return new Network(undirectedSubGraph, superNetwork.categories);
    }
    
}

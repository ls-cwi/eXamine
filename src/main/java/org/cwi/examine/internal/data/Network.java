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
    public final HCategory modules;
    public final double minNodeScore, maxNodeScore;
    public final double minAnnotationScore, maxAnnotationScore;

    public Network() {
        this(new Pseudograph<>(DefaultEdge.class), new ArrayList<>());
    }

    public Network(final UndirectedGraph<HNode, DefaultEdge> graph, final List<HCategory> categories) {
        this.graph = graph;
        this.categories = new ArrayList<>(categories);
        this.categories.removeIf(category -> category.name.equals("Module"));
        this.annotations = new ArrayList<>();
        categories.forEach(category -> annotations.addAll(category.annotations));
        this.modules = categories.stream()
                .filter(category -> category.name.equals("Module"))
                .findFirst()
                .orElse(new HCategory("Module", Collections.emptyList()));

        Set<HNode> nodes = graph.vertexSet();
        minNodeScore = nodes.stream().map(n -> n.score).min(Double::compare).orElse(0.);
        maxNodeScore = nodes.stream().map(n -> n.score).max(Double::compare).orElse(1.);
        minAnnotationScore = annotations.stream().map(a -> a.score).min(Double::compare).orElse(0.);
        maxAnnotationScore = annotations.stream().map(a -> a.score).max(Double::compare).orElse(1.);
    }
    
    /**
     * Induce sub network from super network.
     */
    public static Network induce(final Set<HNode> nodesToInclude, final Network network) {
        // Verify whether entire subset is present in super network.
        nodesToInclude.stream()
                .filter(node -> !network.graph.containsVertex(node))
                .forEach(node -> System.err.println(
                        "Sub network node " + node + " not contained by super network."));

        final Graph<HNode, DefaultEdge> subGraph = new Subgraph(network.graph, nodesToInclude);
        final UndirectedGraph<HNode, DefaultEdge> undirectedSubGraph =
                new UndirectedSubgraph(network.graph, subGraph.vertexSet(), subGraph.edgeSet());

        return new Network(undirectedSubGraph, network.categories);
    }

    public static Network induce(final HCategory categoryToInclude, final Network network) {
        Set<HNode> unionNodes = new HashSet<>();
        categoryToInclude.annotations.forEach(annotation -> unionNodes.addAll(annotation.set));
        return induce(unionNodes, network);
    }

    public static Network induce(final HAnnotation annotationToInclude, final Network network) {
        return induce(annotationToInclude.set, network);
    }
}

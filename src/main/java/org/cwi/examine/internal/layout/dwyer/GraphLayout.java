package org.cwi.examine.internal.layout.dwyer;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cwi.examine.internal.layout.dwyer.cola.Descent;
import org.cwi.examine.internal.layout.dwyer.vpsc.Variable;
import org.jgrapht.Graph;

/**
 * Cola adapter for JGraphT.
 */
public class GraphLayout<V, E> {
    
    public final Graph<V, E> graph;                     // Layed out graph.
    public final Configuration configuration;           // Layout options.
    private Map<V, Point2D.Double> initialPositions;    // Initial vertex positions.
    
    private Map<V, Integer> indexMap;   // Vertex to index map.
    private double[] pX, pY;            // Position coordinate arrays (by vertex index).
    private Variable[] variables;       // Position variables.
    
    private Descent descent;            // Cola gradient descent.
    
    public final Map<V, Point2D.Double> finalPositions; // Final vertex positions.
    
    public GraphLayout(Graph<V, E> graph,
                       Configuration configuration,
                       Map<V, Point2D.Double> initialPositions) {
        this.graph = graph;
        this.configuration = configuration;
        this.initialPositions = initialPositions;
        this.finalPositions = new HashMap<V, Point2D.Double>();
        
        // Initialize cola.
        if(!graph.vertexSet().isEmpty()) {
            initializeCola();
        }
    }
    
    private void initializeCola() {
        // Nodes.
        int N = graph.vertexSet().size();
        indexMap = new HashMap<V, Integer>();
        for(V v: graph.vertexSet()) {
            indexMap.put(v, indexMap.size());
        }
        
        // Positions.
        pX = new double[N];
        pY = new double[N];
        
        if(initialPositions == null) {
            initialPositions = new HashMap<V, Point2D.Double>();
        }
        
        for(V v: graph.vertexSet()) {
            Point2D.Double p = initialPositions.get(v);
            int i = indexMap.get(v);
            
            // Default position is (0,0).
            pX[i] = p == null ? 0 : p.x;
            pY[i] = p == null ? 0 : p.y;
        }
        
        // Variables.
        variables = new Variable[N];
        for(int i = 0; i < N; i++) {
            variables[i] = new Variable(0, 1, 1);
        }
        
        // Vertex to vertex distances.
        
        // Wrap graph edges for distance computation.
        List<ShortestPaths.Edge> distanceEdges = new ArrayList<ShortestPaths.Edge>();
        for(E e: graph.edgeSet()) {
            distanceEdges.add(new DistanceEdge(e, configuration.edgeLength));
        }
        
        // Construct an n X n distance matrix based
        // on shortest paths through graph.
        double[][] D = new ShortestPaths.Calculator(N, distanceEdges).distanceMatrix();
        
        // G is a square matrix with:
        // G[i][j] = 1 iff there exists an edge i <-> j.
        // G[i][j] = 2 iff there exists no edge i <-> j.
        // G[i][j] = small iff there exists no path i <~> j (D[i][j] is inf.),
        // also sets D to a valid value.
        double[][] G = new double[N][N];
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                if(Double.isInfinite(D[i][j])) {
                    //G[i][j] = 2;
                    G[i][j] = 2; //1e-6;
                    //D[i][j] = 3 * configuration.edgeLength;
                } else {
                    G[i][j] = 2;
                }
            }
        }
        for(ShortestPaths.Edge e: distanceEdges) {
            G[e.source()][e.target()] = 1;
            G[e.target()][e.source()] = 1;
        }
        System.out.println("Distances: " + Arrays.deepToString(D));
        
        /*double[][] G = Descent.createSquareMatrix(N, new MatrixFillFunction() {

            @Override
            public double apply(int i, int j) {
                return 2;
            }
            
        });
        for(ShortestPaths.Link e: distanceEdges) {
            G[e.source()][e.target()] = 1;
            G[e.target()][e.source()] = 1;
        }*/
        
        // Gradient descent.
        descent = new Descent(new double[][]{pX, pY}, D, null);

        // Apply initial iterations without user constraints or nonoverlap constraints.
        descent.run(configuration.maxUnconstrainedterations);
        
        // Allow not immediately connected nodes to relax apart (p-stress).
        descent.G = G;
        descent.run(configuration.maxAllConstraintsIterations);
        
        // Spam layout to console.
        for(V v: graph.vertexSet()) {
            int i = indexMap.get(v);
            finalPositions.put(v, new Point2D.Double(pX[i], pY[i]));
        }
    }
    
    
    public static class Configuration {
        public boolean avoidOverlaps = true;                        // Whether to avoid node overlaps.
        public int maxUnconstrainedterations = Integer.MAX_VALUE;   // Maximum unconstrained iterations, 0 => no limit.
        public int maxUserConstraintIterations = Integer.MAX_VALUE; // Maximum user constraint iterations, 0 => no limit.
        public int maxAllConstraintsIterations = Integer.MAX_VALUE; // Maximum all contraints iterations, 0 => no limit.
        public double edgeLength = 40;      // Base edge length.
    }
    
    private class DistanceEdge implements ShortestPaths.Edge {
        
        public final E edge;                // Target edge.
        public final int source, target;    // Source and target vertex indices.
        public final double length;         // Desired edge length in layout.

        public DistanceEdge(E edge, double length) { 
            this.edge = edge;
            this.length = length;
            
            V sV = graph.getEdgeSource(edge);
            V tV = graph.getEdgeTarget(edge);
            source = indexMap.get(sV);
            target = indexMap.get(tV);
        }
        
        @Override
        public int source() {
            return source;
        }

        @Override
        public int target() {
            return target;
        }

        @Override
        public double length() {
            return length;
        }
        
    }
    
}

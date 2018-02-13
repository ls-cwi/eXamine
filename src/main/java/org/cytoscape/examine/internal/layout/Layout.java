package org.cytoscape.examine.internal.layout;

import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.data.Network;
import org.cytoscape.examine.internal.graphics.AnimatedGraphics;
import org.cytoscape.examine.internal.graphics.PVector;
import org.cytoscape.examine.internal.layout.dwyer.cola.Descent;
import org.cytoscape.examine.internal.layout.dwyer.cola.Descent.Projection;
import org.cytoscape.examine.internal.layout.dwyer.vpsc.Constraint;
import org.cytoscape.examine.internal.layout.dwyer.vpsc.Solver;
import org.cytoscape.examine.internal.layout.dwyer.vpsc.Variable;
import org.cytoscape.examine.internal.model.Selection;
import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.alg.PrimMinimumSpanningTree;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.cytoscape.examine.internal.graphics.draw.Constants.LABEL_FONT;
import static org.cytoscape.examine.internal.visualization.Constants.NODE_OUTLINE;
import static org.cytoscape.examine.internal.visualization.Constants.NODE_SPACE;
import static org.cytoscape.examine.internal.visualization.Constants.RIBBON_EXTENT;

public class Layout {
    static final double EDGE_SPACE              = 50;
    static final int    INITIAL_ITERATIONS      = 100000;
    static final int    PHASE_ITERATIONS        = 100;
    static final double SET_EDGE_CONTRACTION    = 0.5;
    
    // Network and set topology.
    public Network network;
    public Selection selection;
    public List<HSet> sets;
    public final HNode[] nodes;
    public final Map<HNode, List<HSet>> nodeMemberships;
    
    // Spanning set graphs.
    private WeightedGraph<HNode, DefaultEdge> minDistGraph;
    private List<Graph<HNode, DefaultEdge>> spanGraphs;
    public WeightedGraph<RichNode, RichEdge> richGraph;
    private WeightedGraph<RichNode, RichEdge> extRichGraph;
    private RichNode[] richNodes;
    
    // Descent layout.
    private Map<HNode, Integer> index;
    private Map<RichNode, Integer> richIndex;
    private double[] baseDilations;
    private double[] radii;
    private double[][] mD;
    private double[][] P;
    private double[][] D;
    private double[][] G;
    private Descent descent;
    
    // Derived metrics.
    public PVector dimensions;
    
    public Layout(AnimatedGraphics g, Network network, Selection selection, Layout oldLayout) {
        this.network = network;
        this.selection = selection;
        
        // Order sets by size.
        this.sets = new ArrayList<HSet>();
        this.sets.addAll(selection.activeSetList);
        Collections.sort(this.sets, new Comparator<HSet>() {
            public int compare(HSet s1, HSet s2) {
                return s1.elements.size() - s2.elements.size();
            }
        });
        
        // Invert set membership for vertices.
        nodes = network.graph.vertexSet().toArray(new HNode[] {});
        nodeMemberships = new HashMap<HNode, List<HSet>>();
        for(HNode n: nodes) {
            nodeMemberships.put(n, new ArrayList<HSet>());
        }
        for(HSet s: sets) {
            for(HNode n: s.elements) {
                nodeMemberships.get(n).add(s);
            }
        }
        
        this.dimensions = PVector.v();
        
        updatePositions(g, oldLayout);
    }
    
    public boolean updatePositions(AnimatedGraphics g) {
        return updatePositions(g,null);
    }
    
    public final boolean updatePositions(AnimatedGraphics g, Layout oldLayout) {
        boolean converged;
        int vN = nodes.length;
            
        if(index == null) {
            index = new HashMap<HNode, Integer>();
            for(int i = 0; i < vN; i++) index.put(nodes[i], i);
            
            // Vertex line radii (width / 2) and base dilations (based on bounds height).
            baseDilations = new double[vN];
            radii = new double[vN];
            for(int i = 0; i < vN; i++) {
                baseDilations[i] = 0.5 * labelSpacedDimensions(g, nodes[i]).y;
                radii[i] = 0.5 * labelSpacedDimensions(g, nodes[i]).x;
            }

            // Vertex to vertex minimum distance (based on set memberships).
            mD = new double[vN][vN];
            for(int i = 0; i < vN; i++) {
                double dil1 = baseDilations[i];
                for(int j = i + 1; j < vN; j++) {
                    double dil2 = baseDilations[j];
                    mD[i][j] = mD[j][i] =
                        dil1 + dil2 + 2 * NODE_SPACE +
                        RIBBON_EXTENT * membershipDiscrepancy(nodes[i], nodes[j]);
                }
            }
            
            // Construct set spanning graphs.
            initializeSetGraphs();
            
            // Update shortest path matrix to rich graph.
            vN = richNodes.length;
            FloydWarshallShortestPaths<RichNode,RichEdge> paths = new FloydWarshallShortestPaths<RichNode,RichEdge>(extRichGraph);
            D = new double[vN][vN];
            for(int i = 0; i < vN; i++)
                for(int j = i + 1; j < vN; j++)
                    D[i][j] = D[j][i] = paths.shortestDistance(richNodes[i], richNodes[j]);
            
            // Vertex positions start at (0,0), or at position of previous layout.
            P = new double[2][vN];
            for(int i = 0; i < nodes.length; i++) {
                PVector pos = oldLayout == null ? PVector.v() : oldLayout.position(richNodes[i]);
                P[0][i] = pos.x;
                P[1][i] = pos.y;
            }
            
            // Gradient descent.
            G = new double[vN][vN];
            for(int i = 0; i < vN; i++)
                for(int j = i; j < vN; j++)
                    G[i][j] = G[j][i] =
                            extRichGraph.containsEdge(richNodes[i], richNodes[j]) ||
                            network.graph.containsEdge(richNodes[i].element, richNodes[j].element) ? 1 : 2;
            descent = new Descent(P, D, null);
            
            // Apply initialIterations without user constraints or non-overlap constraints.
            descent.run(INITIAL_ITERATIONS);
            
            // Initialize vertex and contour bound respecting projection.
            // TODO: convert to rich graph form.
            descent.project = new BoundProjection(radii, mD).projectFunctions();
            
            // Allow not immediately connected (by direction) nodes to relax apart (p-stress).
            descent.G = G;
            descent.run(PHASE_ITERATIONS);
            
            converged = false;
        }
        // Improve layout.
        else {
            converged = descent.run(PHASE_ITERATIONS);
        }
        
        // Measure span and shift nodes top left to (0,0).
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        for(int i = 0; i < vN; i++) {
            minX = Math.min(minX, P[0][i]);
            minY = Math.min(minY, P[1][i]);
            maxX = Math.max(maxX, P[0][i]);
            maxY = Math.max(maxY, P[1][i]);
        }
        this.dimensions = PVector.v(maxX - minX, maxY - minY);
        
        for(int i = 0; i < vN; i++) {
            P[0][i] -= minX;
            P[1][i] -= minY;
        }
        
        return converged;
    }
    
    // Position of the given node, (0,0) iff null.
    public PVector position(HNode node) {
        PVector result;
        
        if(index == null) {
            result = PVector.v();
        } else {
            Integer i = index.get(node);
            result = i == null ? PVector.v() : PVector.v(P[0][i], P[1][i]);
        }
        
        return result;
    }
    
    // Position of the given node, (0,0) iff null.
    public PVector position(RichNode node) {
        PVector result;
        
        if(richIndex == null) {
            result = PVector.v();
        } else {
            Integer i = richIndex.get(node);
            result = i == null ? PVector.v() : PVector.v(P[0][i], P[1][i]);
        }
        
        return result;
    }
    
    private void initializeSetGraphs() {
        int vN = nodes.length;
        
        // Minimum guaranteed distance graph.
        minDistGraph = new SimpleWeightedGraph<HNode, DefaultEdge>(DefaultWeightedEdge.class);
        for(HNode v: network.graph.vertexSet()) {
            minDistGraph.addVertex(v);
        }
        for(DefaultEdge e: network.graph.edgeSet()) {
            HNode s = network.graph.getEdgeSource(e);
            int sI = index.get(s);
            HNode t = network.graph.getEdgeTarget(e);
            int tI = index.get(t);
            DefaultEdge nE = minDistGraph.addEdge(s, t);
            minDistGraph.setEdgeWeight(nE, EDGE_SPACE + mD[sI][tI]);
        }
        
        // Construct shortest path distance matrix on original graph,
        // for distance graph and node overlap constraints.
        FloydWarshallShortestPaths<HNode,DefaultEdge> paths = new FloydWarshallShortestPaths<HNode,DefaultEdge>(minDistGraph);
        D = new double[vN][vN];
        for(int i = 0; i < vN; i++)
            for(int j = i + 1; j < vN; j++)
                D[i][j] = D[j][i] = paths.shortestDistance(nodes[i], nodes[j]);
        
        // Spanning graph per set.
        spanGraphs = new ArrayList<Graph<HNode, DefaultEdge>>();
        for(HSet set: sets) {
            SimpleWeightedGraph<HNode, DefaultEdge> weightedSubGraph =
                    new SimpleWeightedGraph<HNode, DefaultEdge>(DefaultWeightedEdge.class);
            for(HNode v: set.elements) {
                weightedSubGraph.addVertex(v);
            }
            Set<DefaultEdge> coreEdges = new HashSet<DefaultEdge>();
            for(int i = 0; i < set.elements.size(); i++) {
                HNode s = set.elements.get(i);
                for(int j = i + 1; j < set.elements.size(); j++) {
                    HNode t = set.elements.get(j);
                    DefaultEdge nE = weightedSubGraph.addEdge(s, t);
                    
                    // Guarantee MST along already present edges.
                    boolean isCore = network.graph.containsEdge(s, t);
                    weightedSubGraph.setEdgeWeight(nE, isCore ? 0 : D[index.get(s)][index.get(t)]);
                    if(isCore) coreEdges.add(nE);
                }
            }
            
            // Combine spanning and core edges into set spanning graph.
            SimpleGraph<HNode, DefaultEdge> spanGraph =
                    new SimpleGraph<HNode, DefaultEdge>(DefaultEdge.class);
            for(HNode v: set.elements) {
                spanGraph.addVertex(v);
            }
            for(DefaultEdge e: coreEdges) {
                spanGraph.addEdge(weightedSubGraph.getEdgeSource(e),
                                  weightedSubGraph.getEdgeTarget(e));
            }
            
            if(!weightedSubGraph.edgeSet().isEmpty()) {
                Set<DefaultEdge> spanningEdges =
                        new PrimMinimumSpanningTree<HNode, DefaultEdge>(weightedSubGraph)
                            .getMinimumSpanningTreeEdgeSet();
                for(DefaultEdge e: spanningEdges) {
                    spanGraph.addEdge(weightedSubGraph.getEdgeSource(e),
                                      weightedSubGraph.getEdgeTarget(e));
                }
            }
            
            spanGraphs.add(spanGraph);
        }
        
        // Construct rich graph (containing all membership information).
        richGraph = new SimpleWeightedGraph<RichNode, RichEdge>(RichEdge.class);
        richIndex = new HashMap<RichNode, Integer>();
        
        // Base nodes.
        for(int i = 0; i < nodes.length; i++) {
            HNode n = nodes[i];
            RichNode rN = new RichNode(n);
            rN.memberships.addAll(nodeMemberships.get(n));
            richGraph.addVertex(rN);
        }
        // Add all core edges.
        for(DefaultEdge e: network.graph.edgeSet()) {
            RichNode rSN = new RichNode(network.graph.getEdgeSource(e));
            RichNode rTN = new RichNode(network.graph.getEdgeTarget(e));
            RichEdge rE = richGraph.addEdge(rSN, rTN);
            rE.core = true;
            richGraph.setEdgeWeight(rE, D[index.get(rSN.element)][index.get(rTN.element)]);
        }
        // Add all set span edges.
        for(int i = 0; i < sets.size(); i++) {
            HSet s = sets.get(i);
            Graph<HNode, DefaultEdge> sG = spanGraphs.get(i);
            
            for(DefaultEdge e: sG.edgeSet()) {
                RichNode rSN = new RichNode(sG.getEdgeSource(e));
                RichNode rTN = new RichNode(sG.getEdgeTarget(e));
                RichEdge rE = richGraph.addEdge(rSN, rTN);

                if(rE == null) {
                    rE = richGraph.getEdge(rSN, rTN);
                } else {
                    rE.core = false;
                    int rSI = index.get(rSN.element);
                    int rTI = index.get(rTN.element);
                    richGraph.setEdgeWeight(rE, Math.max(mD[rSI][rTI],
                        (SET_EDGE_CONTRACTION / selection.activeSetMap.get(s)) * D[rSI][rTI]));
                }
                //rE.memberships.add(s);
            }
        }
        // Infer edge to set memberships from matching vertices.
        for(RichEdge e: richGraph.edgeSet()) {
            RichNode rSN = richGraph.getEdgeSource(e);
            RichNode rTN = richGraph.getEdgeTarget(e);
            e.memberships.addAll(this.nodeMemberships.get(rSN.element));
            e.memberships.retainAll(this.nodeMemberships.get(rTN.element));
        }
        
        // Construct rich graph that has been extended by one dummy node per edge.
        richNodes = new RichNode[vN + richGraph.edgeSet().size()];
        extRichGraph = new SimpleWeightedGraph<RichNode, RichEdge>(RichEdge.class);
        // Base nodes.
        for(int i = 0; i < nodes.length; i++) {
            HNode n = nodes[i];
            RichNode rN = new RichNode(n);
            richNodes[i] = rN;
            richIndex.put(rN, i);
            extRichGraph.addVertex(rN);
        }
        // Add edges, but include additional dummy node.
        int j = 0;
        for(RichEdge e: richGraph.edgeSet()) {
            RichNode rSN = richGraph.getEdgeSource(e);
            RichNode rTN = richGraph.getEdgeTarget(e);
            
            RichNode dN = new RichNode(null);
            extRichGraph.addVertex(dN);
            e.subNode = dN;
            richNodes[nodes.length + j] = dN;
            richIndex.put(dN, nodes.length + j);
            
            RichEdge sE = extRichGraph.addEdge(rSN, dN);
            sE.core = e.core;
            RichEdge tE = extRichGraph.addEdge(dN, rTN);
            tE.core = e.core;
            
            double hW = 0.5 * richGraph.getEdgeWeight(e);
            extRichGraph.setEdgeWeight(sE, hW);
            extRichGraph.setEdgeWeight(tE, hW);
            
            j++;
        }
    }
    
    // Dimensions of drawn node label.
    public static PVector labelDimensions(AnimatedGraphics g, HNode node, boolean padding) {
        double height = g.textHeight();
        
        g.textFont(LABEL_FONT);
        return PVector.v(g.textWidth(node.toString()) /*+ NODE_OUTLINE*/ + (padding ? height : 0),
                 height + NODE_OUTLINE);
    }
    
    public static PVector labelSpacedDimensions(AnimatedGraphics g, HNode node) {
        return PVector.add(labelDimensions(g, node, true),
                           PVector.v(NODE_OUTLINE + NODE_SPACE, NODE_OUTLINE + NODE_SPACE));
    }
    
    // Set membership discrepancy between two nodes.
    private int membershipDiscrepancy(HNode n1, HNode n2) {
        int discr = 0;
        
        List<HSet> sets1 = nodeMemberships.get(n1);
        List<HSet> sets2 = nodeMemberships.get(n2);
        for(HSet s: sets1)
            if(!s.set.contains(n2))
                discr++;
        for(HSet s: sets2)
            if(!s.set.contains(n1))
                discr++;
        
        return discr;
    }
    
    private class BoundProjection {
        private final Variable[] xVariables, yVariables;
        private final double[] radii;
        private final double[][] distances;

        public BoundProjection(double[] radii, double[][] distances) {
            this.radii = radii;
            this.distances = distances;
            
            xVariables = new Variable[radii.length];
            yVariables = new Variable[radii.length];
            for(int i = 0; i < radii.length; i++) {
                xVariables[i] = new Variable(0, 1, 1);
                yVariables[i] = new Variable(0, 1, 1);
            }
        }

        public Projection[] projectFunctions() {
            return new Projection[] {
                new Projection() {
                    @Override
                    public void apply(double[] x0, double[] y0, double[] r) {
                        xProject(x0, y0, r);
                    }
                },
                new Projection() {
                    @Override
                    public void apply(double[] x0, double[] y0, double[] r) {
                        yProject(x0, y0, r);
                    }
                }
            };
        }

        private void xProject(double[] x0, double[] y0, double[] x) {
            solve(xVariables, createConstraints(x0, y0, true), x0, x);
        }

        private void yProject(double[] x0, double[] y0, double[] y) {
            solve(yVariables, createConstraints(x0, y0, false), y0, y);
        }

        private Constraint[] createConstraints(double[] x0, double[] y0, boolean xAxis) {
           List<Constraint> cs = new ArrayList<Constraint>();

            // Pair wise constraints, only when within distance bounds.
            // Limit to plain nodes, for now.
            for (int i = 0; i < nodes.length; i++) {
                PVector iP = PVector.v(x0[i], y0[i]);

                for (int j = 0; j < nodes.length; j++) {
                    PVector jP = PVector.v(x0[j], y0[j]);

                    double ijDD = this.distances[i][j];  // Desired distance.
                    if(ijDD > Math.abs(y0[i] - y0[j]) || // Rough distance cut optimization.
                       ijDD > Math.abs(x0[i] - x0[j])) {
                        double iR = this.radii[i];
                        double jR = this.radii[j];

                        PVector xM = PVector.v(0.5 * (iP.x + jP.x + (iP.x < jP.x ? this.radii[i] - this.radii[j] :
                                                                           this.radii[j] - this.radii[i])),
                                       0.5 * (iP.y + jP.y)); // Point between two vertex lines.
                        PVector iM = PVector.v(Math.min(iP.x + iR, Math.max(iP.x - iR, xM.x)), iP.y);
                        PVector jM = PVector.v(Math.min(jP.x + jR, Math.max(jP.x - jR, xM.x)), jP.y);
                        PVector ijV = PVector.sub(jM, iM);  // Minimum distance vector between vertex lines.
                        double ijAD = ijV.magnitude();      // Actual distance between vertex lines.

                        // Create constraint when distance is violated.
                        if(ijDD > ijAD) {
                            Variable lV;
                            Variable rV;
                            double gap;

                            // Use ij vector angle to determine axis of constraint.
                            if(xAxis && iM.x != jM.x) {
                                lV = iP.x < jP.x ? xVariables[i] : xVariables[j];
                                rV = iP.x < jP.x ? xVariables[j] : xVariables[i];
                                gap = this.radii[i] + this.radii[j] +
                                      PVector.mul(ijDD, PVector.normalize(ijV)).x;

                                cs.add(new Constraint(lV, rV, gap, false));
                            }

                            if(!xAxis /*&& Math.abs(ijV[0]) < Math.abs(ijV[1])*/) {
                                lV = iP.y < jP.y ? this.yVariables[i] : this.yVariables[j];
                                rV = iP.y < jP.y ? this.yVariables[j] : this.yVariables[i];
                                gap = PVector.mul(ijDD, PVector.normalize(ijV)).y;

                                cs.add(new Constraint(lV, rV, gap, false));
                            }
                        }
                    }
                }
            }

            return cs.toArray(new Constraint[]{});
        }

        private void solve(Variable[] vs,
                           Constraint[] cs,
                           double[] starting,
                           double[] desired) {
            Solver solver = new Solver(vs, cs);
            solver.setStartingPositions(starting);
            solver.setDesiredPositions(desired);
            solver.solve();

            // Push solution as result.
            for(int i = 0; i < vs.length; i++) {
                desired[i] = vs[i].position();
            }
        }
    }
    
    public static class RichNode {
        public HNode element;
        public List<HSet> memberships;

        public RichNode(HNode element) {
            this.element = element;
            this.memberships = new ArrayList<HSet>();
        }

        @Override
        public int hashCode() {
            return element == null ? super.hashCode() : this.element.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            final RichNode other = (RichNode) obj;
            return element == null ? super.equals(obj) : element.equals(other.element);
        }        
    }
    
    @SuppressWarnings("serial")
	public static class RichEdge extends DefaultWeightedEdge {
        public boolean core;            // Whether edge is part of original graph.
        public List<HSet> memberships;  // Set memberships.
        public RichNode subNode;        // Optional dummy node that divides edge in extended graph.
        
        public RichEdge() {
            memberships = new ArrayList<HSet>();
        }
    }
}

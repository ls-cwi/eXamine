package org.cwi.examine.internal.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cwi.examine.internal.data.Network;
import org.cwi.examine.internal.graphics.PVector;
import org.cwi.examine.internal.layout.dwyer.cola.Descent;
import org.cwi.examine.internal.layout.dwyer.vpsc.Constraint;


import org.cwi.examine.internal.molepan.dataread.DataRead;

//import org.cwi.examine.internal.layout.mp.MolecularPartitioner;


import org.cwi.examine.internal.visualization.Parameters;
import org.cwi.examine.internal.data.HNode;
import org.cwi.examine.internal.data.HAnnotation;
import org.cwi.examine.internal.graphics.StaticGraphics;
import org.cwi.examine.internal.layout.dwyer.vpsc.Variable;
import org.cwi.examine.internal.layout.dwyer.vpsc.Solver;
import org.cwi.examine.internal.model.Selection;
import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.alg.PrimMinimumSpanningTree;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import static org.cwi.examine.internal.graphics.StaticGraphics.*;

public class Layout {
    static final double EDGE_SPACE              = 50;
    static final int    INITIAL_ITERATIONS      = 100000;
    static final int    PHASE_ITERATIONS        = 10000;
    static final double SET_EDGE_CONTRACTION    = 0.5;
    
    // Network and set topology.
    public Network network;
    public Selection selection;
    public List<HAnnotation> sets;
    public final HNode[] nodes;
    public final Map<HNode, List<HAnnotation>> nodeMemberships;
    
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
    
      private double[][] Temp;
    
    private double[][] D;
    private double[][] G;
    private Descent descent;
   
    // Derived metrics.
    public PVector dimensions;
    
    public Layout(Network network, Selection selection, Layout oldLayout) {
        this.network = network;
        this.selection = selection;
        
        // Order annotations by size.
        this.sets = new ArrayList<>();
        this.sets.addAll(selection.activeSetList);
        Collections.sort(this.sets, (s1, s2) -> s1.elements.size() - s2.elements.size());
        
        // Invert set membership for vertices.
        nodes = network.graph.vertexSet().toArray(new HNode[] {});
        nodeMemberships = new HashMap<>();
        for(HNode n: nodes) {
            nodeMemberships.put(n, new ArrayList<>());
        }
        for(HAnnotation s: sets) {
            for(HNode n: s.elements) {
                nodeMemberships.get(n).add(s);
            }
        }
        
        
      
        this.dimensions = PVector.v();
        
     
        //MolecularPartitioner mp = new MolecularPartitioner(network);
      
        
        
        updatePositions(oldLayout);
    }
    
    public boolean updatePositions() {
        return updatePositions(null);
    }
    
    public final boolean updatePositions(Layout oldLayout) {
        boolean converged;
        int vN = nodes.length;
            
        if(index == null) {
            index = new HashMap<>();
            for(int i = 0; i < vN; i++) index.put(nodes[i], i);
            
            // Vertex line radii (width / 2) and base dilations (based on bounds height).
            baseDilations = new double[vN];
            radii = new double[vN];
            for(int i = 0; i < vN; i++) {
                baseDilations[i] = 0.5 * labelSpacedDimensions(nodes[i]).y;
                radii[i] = 0.5 * labelSpacedDimensions(nodes[i]).x;
            }

            // Vertex to vertex minimum distance (based on set memberships).
            mD = new double[vN][vN];
            for(int i = 0; i < vN; i++) {
                double dil1 = baseDilations[i];
                for(int j = i + 1; j < vN; j++) {
                    double dil2 = baseDilations[j];
                    mD[i][j] = mD[j][i] =
                        dil1 + dil2 + 2 * Parameters.NODE_SPACE +
                        Parameters.RIBBON_EXTENT * membershipDiscrepancy(nodes[i], nodes[j]);
                }
            }
            
            // Construct set spanning graphs.
            initializeSetGraphs();
            
            // Update shortest path matrix to rich graph.
            vN = richNodes.length;
            FloydWarshallShortestPaths paths = new FloydWarshallShortestPaths(extRichGraph);
            D = new double[vN][vN];
            for(int i = 0; i < vN; i++)
                for(int j = i + 1; j < vN; j++)
                    D[i][j] = D[j][i] = paths.shortestDistance(richNodes[i], richNodes[j]);
            
            // Vertex positions start at (0,0), or at position of previous layout.
            P = new double[2][vN];
            for(int i = 0; i < nodes.length; i++) {
                PVector pos = oldLayout == null ? PVector.v() : oldLayout.position(richNodes[i]);
                P[0][i] = pos.x;
                P[1][i] =  pos.y;
                

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
    /*    
        for(int i = 0; i < vN; i++) {
            minX = Math.min(minX, P[0][i]);
            minY = Math.min(minY, P[1][i]);
            maxX = Math.max(maxX, P[0][i]);
            maxY = Math.max(maxY, P[1][i]);
        }
        this.dimensions = PVector.v(maxX - minX, maxY - minY); */
        
          
        
        	  for(int i = 0; i < vN; i++) {
    
            P[1][i] = DataRead.coordinates[1][(DataRead.PosAtom [i % DataRead.atomNo]-1) ]*40; 
            
            
      		P[0][i] =  DataRead.coordinates[0][(DataRead.PosAtom [i % DataRead.atomNo]-1) ]*40; 
 					//System.out.println("test " + DataRead.PosAtom [i-1 % (DataRead.atomNo)]);
       			 }
       
         //System.out.println(vN);
      

    double fac = 40;

/*
P[0][9] =-2.83*fac; P[1][9] = 0.95*fac;  //1
P[0][10] =-2.83*fac; P[1][10] = -0.55*fac; //2
P[0][11] =-1.53*fac; P[1][11] = -1.29*fac; //3
P[0][12] =-0.24*fac; P[1][12] = -0.55*fac; //4


P[0][13] =-0.24*fac; P[1][13] = 0.95*fac; //5
P[0][14] =1.06*fac; P[1][14] = 1.71*fac;



P[0][15] =2.36*fac; P[1][15] = 0.95*fac;

P[0][16] =2.36*fac; P[1][16] = -0.55*fac;
P[0][17] =1.06*fac; P[1][17] = -1.29*fac;


P[0][18] =2.36*fac; P[1][18] = -2.04*fac;


P[0][0] =3.110208131003807*fac; P[1][0] = 2.048917918952455*fac;


P[0][1] =3.110208131003807*fac; P[1][1] = -3.3389179189524545*fac;

P[0][2] =4.610208111749353*fac; P[1][2] = -3.338677578853741*fac;

P[0][3] =5.360416242753159*fac; P[1][3] = -4.637595497806196*fac;

P[0][4] =6.860416223498705*fac; P[1][4] = -4.637355157707486*fac;

P[0][5] =7.610624354502509*fac; P[1][5] = -5.936273076659942*fac;


P[0][6] =4.610624393011419*fac; P[1][6] = -5.936273076659942*fac;

P[0][7] =4.610624393011419*fac; P[1][7] = -7.437586839572579*fac;
P[0][8] =3.3112256603763273*fac; P[1][8] = -6.68779498983084*fac;
        




1 (-2.83, 0.95)
2 (-2.83, -0.55)
3 (-1.53, -1.29)
4 (-0.24, -0.55)
5 (-0.24, 0.95)
6 (1.06, 1.71)
7 (2.36, 0.95)
8 (2.36, -0.55)
9 (1.06, -1.29)
10 (2.36, -2.04

11 (3.110208131003807, -3.3389179189524545)
12 (4.610208111749353, -3.338677578853741)
13 (5.360416242753159, -4.637595497806196)
14 (6.860416223498705, -4.637355157707486)
15 (7.610624354502509, -5.936273076659942)
16 (4.610624393011419, -5.936753756857364)
17 (4.610383919427495, -7.437586839572579)
18 (3.3112256603763273, -6.68779498983084)        
        
) */
        
      
        
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
        FloydWarshallShortestPaths paths = new FloydWarshallShortestPaths(minDistGraph);
        D = new double[vN][vN];
        for(int i = 0; i < vN; i++)
            for(int j = i + 1; j < vN; j++)
                D[i][j] = D[j][i] = paths.shortestDistance(nodes[i], nodes[j]);
        
        // Spanning graph per set.
        spanGraphs = new ArrayList<Graph<HNode, DefaultEdge>>();
        for(HAnnotation set: sets) {
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
            HAnnotation s = sets.get(i);
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
    public static PVector labelDimensions(HNode node, boolean padding) {
        double height = textHeight();
        
        StaticGraphics.textFont(org.cwi.examine.internal.graphics.draw.Parameters.labelFont);
        return PVector.v(textWidth(node.toString()) /*+ NODE_OUTLINE*/ + (padding ? height : 0),
                 height + Parameters.NODE_OUTLINE);
    }
    
    public static PVector labelSpacedDimensions(HNode node) {
        return PVector.add(labelDimensions(node, true),
                           PVector.v(Parameters.NODE_OUTLINE + Parameters.NODE_SPACE, Parameters.NODE_OUTLINE + Parameters.NODE_SPACE));
    }
    
    // Set membership discrepancy between two nodes.
    private int membershipDiscrepancy(HNode n1, HNode n2) {
        int discr = 0;
        
        List<HAnnotation> sets1 = nodeMemberships.get(n1);
        List<HAnnotation> sets2 = nodeMemberships.get(n2);
        for(HAnnotation s: sets1)
            if(!s.set.contains(n2))
                discr++;
        for(HAnnotation s: sets2)
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

        public Descent.Projection[] projectFunctions() {
            return new Descent.Projection[] {
                new Descent.Projection() {
                    @Override
                    public void apply(double[] x0, double[] y0, double[] r) {
                        xProject(x0, y0, r);
                    }
                },
                new Descent.Projection() {
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
        public List<HAnnotation> memberships;

        public RichNode(HNode element) {
            this.element = element;
            this.memberships = new ArrayList<HAnnotation>();
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
    
    public static class RichEdge extends DefaultWeightedEdge {
        public boolean core;            // Whether edge is part of original graph.
        public List<HAnnotation> memberships;  // Set memberships.
        public RichNode subNode;        // Optional dummy node that divides edge in extended graph.
        
        public RichEdge() {
            memberships = new ArrayList<HAnnotation>();
        }
    }
}

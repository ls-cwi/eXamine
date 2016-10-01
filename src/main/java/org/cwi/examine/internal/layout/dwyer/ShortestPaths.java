package org.cwi.examine.internal.layout.dwyer;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShortestPaths {
    
    //Edges passed into Calculator constructor must have the following properties:
    public static interface Edge {
        public int source();
        public int target();
        public double length();
    }

    public static class Neighbour {
        public int id;
        public double distance;
        
        public Neighbour(int id, double distance) {
            this.id = id;
            this.distance = distance;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + this.id;
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
            final Neighbour other = (Neighbour) obj;
            if (this.id != other.id) {
                return false;
            }
            return true;
        }
        
    }

    public static class Node {
        public int id;
        public Set<Neighbour> neighbours;
        public double d;
        public PairingHeap<Node> q;
        
        public Node(int id) {
            this.id = id;
            this.neighbours = new HashSet<Neighbour>();
        }
        
    }
    
    public static Comparator<Node> distanceComparator = new Comparator<Node>() {

        @Override
        public int compare(Node left, Node right) {
            double diff = left.d - right.d;
            
            return diff < 0 ? -1 : (diff > 0 ? 1 : 0);
        }
        
    };
    
            
    public static class Calculator {
        
        private Node[] neighbours;
        private int n;

        public Calculator(int n, List<Edge> es) {
            this.n = n;
            this.neighbours = new Node[n];
            for(int i = n - 1; i >= 0; i--) neighbours[i] = new Node(i);

            for(int i = es.size() - 1; i >= 0; i--) {
                Edge e = es.get(i);
                int u = e.source();
                int v = e.target();
                double d = Double.isNaN(e.length()) ? 1 : e.length();
                this.neighbours[u].neighbours.add(new Neighbour(v, d));
                this.neighbours[v].neighbours.add(new Neighbour(u, d));
            }
        }
        
        double[][] distanceMatrix() {
            double[][] d = new double[n][];
            for (int i = 0; i < n; i++) {
                d[i] = dijkstraNeighbours(i);
            }
            return d;
        }
        
        double[] distancesFromNode(int start) {
            return dijkstraNeighbours(start);
        }

        private double[] dijkstraNeighbours(int start) {
            PriorityQueue<Node> q = new PriorityQueue<Node>(ShortestPaths.distanceComparator);
            
            double[] d = new double[neighbours.length];
            for(int i = neighbours.length - 1; i >= 0; i--) {
                Node node = this.neighbours[i];
                node.d = i == start ? 0 : Double.POSITIVE_INFINITY;
                node.q = q.push(node);
            }
            
            while (!q.empty()) {
                Node u = q.pop();
                
                d[u.id] = u.d;
                for(Neighbour neighbour: u.neighbours) {
                    Node v = this.neighbours[neighbour.id];
                    double t = u.d + neighbour.distance;
                    
                    if (!Double.isNaN(u.d) && v.d > t) {
                        v.d = t;
                        v.q = q.reduceKey(v.q, v);
                    }
                }
            }
            
            return d;
        }
    }
}
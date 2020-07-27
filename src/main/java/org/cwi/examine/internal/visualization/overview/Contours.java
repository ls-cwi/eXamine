package org.cwi.examine.internal.visualization.overview;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cwi.examine.internal.data.HNode;
import org.cwi.examine.internal.data.HAnnotation;
import org.cwi.examine.internal.graphics.PVector;
import org.cwi.examine.internal.layout.Layout;
import org.cwi.examine.internal.layout.Layout.RichEdge;
import org.cwi.examine.internal.layout.Layout.RichNode;
import org.cwi.examine.internal.visualization.Util;

import static org.cwi.examine.internal.visualization.Parameters.*;
import static org.cwi.examine.internal.visualization.Util.geometryFactory;

// Generates set contours for a SOM.
public class Contours {
    public Layout layout;  // Node layout.
    
    // Set body and outline shapes, per set index.
    public final List<Geometry> ribbonShapes, outlineShapes;
    
    public Contours(Layout layout) {
        this.layout = layout;
        this.ribbonShapes = new ArrayList<Geometry>();
        this.outlineShapes = new ArrayList<Geometry>();
        
        // Compute contour shapes.
        for(HAnnotation set: layout.sets) {
            deriveContour(set, layout);
        }
    }
    
    private void deriveContour(HAnnotation set, Layout layout) {
        // Radius for smoothening contours.
        double smoothRadius = 4 * RIBBON_EXTENT;

        List<Geometry> vertexHulls = new ArrayList<Geometry>();
        for(HNode v: set.elements) {
            // Radius of set around vertex.
            double vertexIndex = 1.01 + layout.nodeMemberships.get(v).indexOf(set);
            double edgeRadius = vertexIndex * RIBBON_EXTENT + smoothRadius;

            // Radius of vertex (assuming rounded rectangle).
            PVector vertexBounds = Layout.labelDimensions(v, false);
            PVector vertexPos = layout.position(v);
            double vertexRadius = 0.5 * vertexBounds.y + NODE_MARGIN;
            double totalRadius = vertexRadius + edgeRadius;

            Geometry line = geometryFactory.createLineString(
                new Coordinate[] {
                    new Coordinate(vertexPos.x - 0.5 * vertexBounds.x, vertexPos.y),
                    new Coordinate(vertexPos.x + 0.5 * vertexBounds.x, vertexPos.y)
                });
            Geometry hull = line.buffer(totalRadius, BUFFER_SEGMENTS);

            vertexHulls.add(hull);
        }
        
        List<Geometry> linkHulls = new ArrayList<Geometry>();
        for(RichEdge e: layout.richGraph.edgeSet()) {
            int ind = e.memberships.indexOf(set);
            
            if(ind >= 0) {
                RichNode sN = layout.richGraph.getEdgeSource(e);
                PVector sP = layout.position(sN.element);
                RichNode tN = layout.richGraph.getEdgeTarget(e);
                PVector tP = layout.position(tN.element);
                RichNode dN = e.subNode;
                PVector dP = layout.position(dN);
                boolean hasCore = layout.network.graph.containsEdge(sN.element, tN.element);
                
                // Radius of set around vertex.
                double edgeIndex = 0.51 + ind;
                double edgeRadius = edgeIndex * RIBBON_EXTENT + smoothRadius +
                        (hasCore ? LINK_WIDTH + RIBBON_SPACE : 0);  // Widen for contained edge.

                Geometry line = Util.circlePiece(sP, dP, tP, LINK_SEGMENTS);
                Geometry hull = line.buffer(edgeRadius, BUFFER_SEGMENTS);

                linkHulls.add(hull);
            }
        }

        // Vertex anti-membership hulls.
        Set<HNode> antiVertices = new HashSet<HNode>(layout.network.graph.vertexSet());
        antiVertices.removeAll(set.elements);
        List<Geometry> vertexAntiHulls = new ArrayList<Geometry>();
        for(HNode v: antiVertices) {
            // Radius of vertex (assuming rounded rectangle).
            PVector bounds = Layout.labelDimensions(v, false);
            PVector pos = layout.position(v);
            double radius = 0.5 * bounds.y + NODE_OUTLINE;

            Geometry line = geometryFactory.createLineString(
                new Coordinate[] {
                    new Coordinate(pos.x - 0.5 * bounds.x, pos.y),
                    new Coordinate(pos.x + 0.5 * bounds.x, pos.y)
                });
            Geometry hull = line.buffer(radius, BUFFER_SEGMENTS);

            vertexAntiHulls.add(hull);
        }

        Geometry vertexContour = convexHulls(Util.fastUnion(vertexHulls));
        Geometry linkContour = Util.fastUnion(linkHulls);
        Geometry fullContour = vertexContour.union(linkContour);
        Geometry smoothenedContour = fullContour.buffer(-smoothRadius, BUFFER_SEGMENTS);

        if (!vertexAntiHulls.isEmpty()) {
            Geometry antiContour = new CascadedPolygonUnion(vertexAntiHulls).union();
            smoothenedContour = smoothenedContour.difference(antiContour);
             // Safeguard link contours, TODO: fix anti-hull vs link cases.
            smoothenedContour = smoothenedContour.union(
                    linkContour.buffer(-smoothRadius, BUFFER_SEGMENTS));
        }

        Geometry innerContour = smoothenedContour.buffer(-RIBBON_WIDTH, BUFFER_SEGMENTS);
        Geometry ribbon = smoothenedContour.difference(innerContour);
        
        ribbonShapes.add(ribbon);
        outlineShapes.add(smoothenedContour);
    }
    
    private static Geometry convexHulls(Geometry g) {
        int gN = g.getNumGeometries();
        
        List<Geometry> sG = new ArrayList<Geometry>();
        for(int i = 0; i < gN; i++) {
            sG.add(g.getGeometryN(i).convexHull());
        }
        
        return new CascadedPolygonUnion(sG).union();
    }
}

package org.cwi.examine.internal.visualization;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import org.cwi.examine.internal.graphics.PVector;
import org.cwi.examine.internal.graphics.StaticGraphics;

// Visualization utility functions.
public class Util {
    public static GeometryFactory geometryFactory = new GeometryFactory();  // JTS geometry factory.

    // Convert a JTS geometry to a Java shape.
    public static Shape geometryToShape(Geometry geometry) {
        return geometryToShape(geometry, 0);
    }
    
    public static Shape geometryToShape(Geometry geometry, double arcFactor) {
        Path2D.Double shape = new Path2D.Double();
        
        geometryToShape(geometry, shape, arcFactor);
        
        return shape;
    }
    
    private static void geometryToShape(Geometry geometry, Path2D path, double arcFactor) {
        if(geometry.getNumGeometries() > 1) {
            for(int i = 0; i < geometry.getNumGeometries(); i++) {
                geometryToShape(geometry.getGeometryN(i), path, arcFactor);
            }
        } else if(geometry instanceof Polygon) {
            polygonToShape((Polygon) geometry, path, arcFactor);
        }
    }    

    // Attach JTS polygon to a shape.
    private static void polygonToShape(Polygon polygon, Path2D path, double arcFactor) {
        // Exterior ring.
        ringToShape(polygon.getExteriorRing(), path, arcFactor);

        // Interior rings.
        for(int i = 0; i < polygon.getNumInteriorRing(); i++) {
            ringToShape(polygon.getInteriorRingN(i), path, arcFactor);
        }
    }

    // Attach JTS ring to a shape.
    private static void ringToShape(LineString string, Path2D path, double arcFactor) {
        Coordinate[] cs = string.getCoordinates();
        
        // Derive smooth arcs from sampled JTS arcs.
        if(arcFactor > 0) {
            PVector[] vs = new PVector[cs.length];
            for(int i = 0; i < cs.length; i++) {
                vs[i] = PVector.v(cs[cs.length - i - 1]);
            }
            
            // Similar region break points.
            List<Integer> breakPoints = new ArrayList<Integer>();
            for(int i = 0; i < vs.length-1; i++) {
                PVector l = vs[i];
                int mI = (i + 1) % (vs.length-1);
                PVector m1 = vs[mI];
                PVector m2 = vs[(i+2) % (vs.length-1)];
                PVector r = vs[(i+3) % (vs.length-1)];
                
                PVector lm = PVector.sub(m1, l);
                PVector m = PVector.sub(m2, m1);
                PVector mr = PVector.sub(r, m2);
                if(Math.abs(PVector.angle(lm, m) - PVector.angle(m, mr)) > arcFactor * Math.PI) {
                    breakPoints.add(mI);
                }
            }
            
            // Contruct path from similar regions.
            for(int i = 0; i < breakPoints.size(); i++) {
                // breakpoint to ... + 1 is a straight line.
                int firstBreak = breakPoints.get(i);
                PVector begin = vs[firstBreak % (vs.length-1)];
                
                // breakpoint to next breakpoint is arc, iff applicable.
                int nextBreak = breakPoints.get((i+1) % breakPoints.size()) % (vs.length-1);
                int bD;
                for(bD = 0; (firstBreak + bD) % (vs.length-1) != nextBreak; bD++) {}
                bD /= 2;
                int midC = (firstBreak + bD) % (vs.length-1);
                PVector mid = vs[midC];
                PVector end = vs[nextBreak];

                path.append(StaticGraphics.getArc(begin, mid, end), true);
            }
                
            path.closePath();
        }
        // Path according to JTS samples.
        else {
            path.moveTo(cs[0].x, cs[0].y);
            for(int j = 1; j < cs.length /*- 1*/; j++) {
                path.lineTo((double) cs[j].x, (double) cs[j].y);
            }
            path.closePath();
        }
    }
    
    // Shorthand for a fast and safe JTS union.
    public static Geometry fastUnion(List<Geometry> gs) {
        return gs.isEmpty() ?
                geometryFactory.createGeometryCollection(new Geometry[] {}) :
                new CascadedPolygonUnion(gs).union();
    }
    
    public static LineString circlePiece(PVector p1, PVector p2, PVector p3, int segments) {
        LineString lS;
        
        PVector v21 = PVector.sub(p2, p1);
        double d21 = v21.dot(v21);
        PVector v31 = PVector.sub(p3, p1);
        double d31 = v31.dot(v31);
        double a4 = 2 * v21.crossZ(v31);
        
        //double a123 = Math.abs(PVector.angle(p1, p2, p3));
        double d13 = PVector.distance(p1, p3);
        boolean wellFormed = PVector.distance(p1, p2) < d13 &&
                             PVector.distance(p2, p3) < d13;
        
        if(wellFormed /*a123 > 0.001 * Math.PI*/ && Math.abs(a4) > 0.001) {
            PVector center = new PVector(p1.x + (v31.y*d21-v21.y*d31)/a4,
                                         p1.y + (v21.x*d31-v31.x*d21)/a4);
            double radius =
                    Math.sqrt(d21*d31*(
                            Math.pow(p3.x-p2.x,2) +
                            Math.pow(p3.y-p2.y,2))) /
                    Math.abs(a4);
            
            double a1 = PVector.sub(p1, center).heading2D();
            double a2 = PVector.sub(p2, center).heading2D();
            double a3 = PVector.sub(p3, center).heading2D();
            if((a2 < a1 && a2 < a3) || (a2 > a1 && a2 > a3)) {
                if(a1 < a3) a3 -= 2 * Math.PI;
                else a1 -= 2 * Math.PI;
            }
            
            Coordinate[] cs = new Coordinate[segments];
            for(int i = 0; i < segments; i++) {
                double fI = (double) i / (double) (segments - 1);
                double aI = (1 - fI) * a1 + fI * a3;
                PVector vI = PVector.fromAngle(aI);
                vI.mult(radius);
                vI.add(center);
                cs[i] = new Coordinate(vI.x, vI.y);
            }
            
            lS = geometryFactory.createLineString(cs);
        }
        // There is no circle, so take a straight line between p0 and p1.
        else {
            Coordinate[] cs = new Coordinate[2];
            cs[0] = new Coordinate(p1.x, p1.y);
            cs[1] = new Coordinate(p3.x, p3.y);
            lS = geometryFactory.createLineString(cs);
        }
        
        return lS;
    }
}

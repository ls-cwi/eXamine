package org.cytoscape.examine.internal.visualization;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Shape;
import java.awt.geom.Path2D;

/**
 * Visualization utility functions.
 */
public class Util {
    
    // JTS geometry factory.
    public static GeometryFactory geometryFactory = new GeometryFactory();

    // Convert a JTS geometry to a Java shape.
    public static Shape geometryToShape(Geometry geometry) {
        Path2D.Double shape = new Path2D.Double();
        
        geometryToShape(geometry, shape);
        
        return shape;
    }
    
    private static void geometryToShape(Geometry geometry, Path2D path) {
        if(geometry.getNumGeometries() > 1) {
            for(int i = 0; i < geometry.getNumGeometries(); i++) {
                geometryToShape(geometry.getGeometryN(i), path);
            }
        } else if(geometry instanceof Polygon) {
            polygonToShape((Polygon) geometry, path);
        }
    }    

    // Attach JTS polygon to a shape.
    private static void polygonToShape(Polygon polygon, Path2D path) {
        // Exterior ring.
        ringToShape(polygon.getExteriorRing(), path);

        // Interior rings.
        for(int i = 0; i < polygon.getNumInteriorRing(); i++) {
            ringToShape(polygon.getInteriorRingN(i), path);
        }
    }

    // Attach JTS ring to a shape.
    private static void ringToShape(LineString string, Path2D path) {
        Coordinate[] cs = string.getCoordinates();
        
        path.moveTo(cs[0].x, cs[0].y);
        for(int j = 1; j < cs.length - 1; j++) {
            path.lineTo((double) cs[j].x, (double) cs[j].y);
        }
        path.closePath();
    }
    
}

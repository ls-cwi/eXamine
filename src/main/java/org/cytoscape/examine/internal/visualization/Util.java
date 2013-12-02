package org.cytoscape.examine.internal.visualization;

import static aether.Aether.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import static processing.core.PConstants.*;

/**
 * Visualization utility functions.
 */
public class Util {
    
    // JTS geometry factory.
    public static GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Convert a JTS geometry to a processing shape, given its purpose:
     * to draw the contour, or the body.
     */
    public static void drawGeometry(Geometry geometry, boolean contour) {
        if(geometry.getNumGeometries() > 1) {
            for(int i = 0; i < geometry.getNumGeometries(); i++) {
                drawGeometry(geometry.getGeometryN(i), contour);
            }
        } else if(geometry instanceof Polygon) {
            polygonToShape((Polygon) geometry, contour);
        }
    }    

    /**
     * Attach JTS polygon to a shape.
     */
    private static void polygonToShape(Polygon polygon, boolean contour) {
        beginShape();
        ringToVertices(polygon.getExteriorRing());

        // A body shape has holes.
        if(!contour) {
            for(int i = 0; i < polygon.getNumInteriorRing(); i++) {
                beginContour();
                ringToVertices(polygon.getInteriorRingN(i));
                endContour();
            }
        }
        
        endShape(CLOSE);
        
        // A contour has more line loops.
        if(contour) {
            for(int i = 0; i < polygon.getNumInteriorRing(); i++) {
                beginShape();
                ringToVertices(polygon.getInteriorRingN(i));
                endShape(CLOSE);
            }
        }
    }

    /**
     * Attach JTS ring to a shape.
     */
    private static void ringToVertices(LineString string) {
        Coordinate[] cs = string.getCoordinates();
        for(int j = 0; j < cs.length - 1; j++) {
            vertex((float) cs[j].x, (float) cs[j].y);
        }
    }
    
}

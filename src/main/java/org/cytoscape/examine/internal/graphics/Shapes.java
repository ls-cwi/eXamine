package org.cytoscape.examine.internal.graphics;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

import static org.cytoscape.examine.internal.graphics.PVector.sub;

public final class Shapes {

    /**
     * Prevent instantiation of utility class.
     */
    private Shapes() {

    }

    public static Shape getArc(PVector p1, PVector p2, PVector p3) {
        Shape path;

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

            Arc2D arc = new Arc2D.Double(Arc2D.OPEN);
            arc.setFrame(center.x - radius, center.y - radius,
                    2 * radius, 2 * radius);

            boolean cross = sub(p2, p1).crossZ(sub(p3, p2)) < 0;
            if(cross) arc.setAngles(p1.x, p1.y, p3.x, p3.y);
            else {
                arc.setAngles(p3.x, p3.y, p1.x, p1.y);
                double extent = arc.getAngleExtent();
                arc.setAngleStart(arc.getAngleStart() + extent);
                arc.setAngleExtent(-extent);
            }

            path = arc;
        }
        // There is no circle, so take a straight line between p0 and p1.
        else {
            Path2D.Double line = new Path2D.Double();
            line.moveTo(p1.x, p1.y);
            line.lineTo(p3.x, p3.y);

            path = line;
        }

        return path;
    }

}

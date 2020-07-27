package org.cwi.examine.internal.graphics;

import com.vividsolutions.jts.geom.Coordinate;
import static java.lang.Math.*;

// Two dimensional vector.
public class PVector {
    public double x, y;

    public PVector() {
        this.x = 0;
        this.y = 0;
    }
    
    public PVector(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public static PVector fromAngle(double angle) {
        return new PVector(cos(angle), sin(angle));
    }
    
    // Set vector to given components.
    public void set(PVector v) {
        this.x = v.x;
        this.y = v.y;
    }
    
    // Get a copy.
    public PVector get() {
        return new PVector(x, y);
    }
    
    // Vector magnitude (L2).
    public double magnitude() {
        return sqrt(x * x + y * y);
    }
    
    // Multiply by given scalar.
    public void mult(double s) {
        x *= s;
        y *= s;
    }
    
    // Divide by given scalar.
    public void div(double s) {
        x /= s;
        y /= s;
    }
    
    // Add given components to vector.
    public void add(double dx, double dy) {
        x += dx;
        y += dy;
    }
    
    // Add two vectors.
    public static PVector add(PVector l, PVector r) {
        return new PVector(l.x + r.x, l.y + r.y);
    }
    
    // Add vectors.
    public void add(PVector that) {
        x += that.x;
        y += that.y;
    }
    
    // Subtract given vector.
    public void sub(PVector that) {
        x -= that.x;
        y -= that.y;
    }
    
    // Dot product with the given vector.
    public double dot(PVector that) {
        return this.x * that.x + this.y * that.y;
    }
    
    // Cross product with the given vector.
    public double crossZ(PVector that) {
        return this.x * that.y - that.x * this.y;
    }
    
    // Euclidian distance between two vectors.
    public static double distance(PVector v1, PVector v2) {
        return sub(v1, v2).magnitude();
    }
    
    // Subtract one vector from another
    public static PVector sub(PVector v, PVector toSubtract) {
        return new PVector(v.x - toSubtract.x, v.y - toSubtract.y);
    }
    
    // Multiply given vector by given scalar, returning a new vector.
    public static PVector mul(double s, PVector v) {
        return new PVector(s * v.x, s * v.y);
    }
    
    // Counter clock-wise rotate vector.
    public void rotate(double angle) {
        x = x * cos(angle) - y * sin(angle);
        y = x * sin(angle) + y * cos(angle);
    }
    
    // Get the heading (angle) of the vector.
    public double heading2D() {
        return atan2(y, x);
    }
    
    // Get the angle between two vectors.
    public static double angle(PVector v1, PVector v2) {
        return v1.heading2D() - v2.heading2D();
    }
    
    // Get the angle between three points as vectors.
    public static double angle(PVector v1, PVector v2, PVector v3) {
        return angle(sub(v2,v1), sub(v3,v1));
    }
    
    // Normalize this vector.
    public void normalize() {
        double magnitude = magnitude();
        x /= magnitude;
        y /= magnitude;
    }
    
    // Normalize vector.
    public static PVector normalize(PVector v) {
        double magnitude = v.magnitude();
        return new PVector(v.x / magnitude, v.y / magnitude);
    }
    
    // Project vector to X axis.
    public PVector X() {
        return new PVector(x, 0);
    }
    
    // Project vector to Y axis.
    public PVector Y() {
        return new PVector(0, y);
    }
    
    // Get the right-sided orthogonal of a 2D vector,
    // preserving the magnitude of the given vector.
    public PVector rightOrthogonal() {
        return new PVector(y, -x);
    }
    
    // Get the left-sided orthogonal of a 2D vector,
    // preserving the magnitude of the given vector.
    public PVector leftOrthogonal() {
        return new PVector(-y, x);
    }

    // Create 0D PVector.
    public static PVector v() {
        return new PVector();
    }

    // Create 2D PVector.
    public static PVector v(double x, double y) {
        return new PVector(x, y);
    }
    
    // Convert JTS coordinate to vector.
    public static PVector v(Coordinate c) {
        return new PVector(c.x, c.y);
    }
    public static PVector[] v(Coordinate[] cs) {
        PVector[] vs = new PVector[cs.length];
        for(int i = 0; i < cs.length; i++) {
            vs[i] = v(cs[i]);
        }
        return vs;
    }
}

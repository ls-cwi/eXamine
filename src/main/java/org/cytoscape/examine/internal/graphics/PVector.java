package org.cytoscape.examine.internal.graphics;

import static java.lang.Math.*;

/**
 * Two dimensional vector.
 */
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
    
    // Subtract one vector from another
    public static PVector sub(PVector v, PVector toSubtract) {
        return new PVector(v.x - toSubtract.x, v.y - toSubtract.y);
    }
    
    // Counter clock-wise rotate vector.
    public void rotate(double angle) {
        x = x * cos(angle) - y * sin(angle);
        y = x * sin(angle) + y * cos(angle);
    }
    
    // Get the heading (angle) of the vector.
    public double heading2D() {
        return atan2(x, y);
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
    
}

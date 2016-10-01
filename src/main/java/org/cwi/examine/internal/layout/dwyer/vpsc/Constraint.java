package org.cwi.examine.internal.layout.dwyer.vpsc;

public class Constraint {
    public double lm;
    public boolean active = false;
    public boolean unsatisfiable = false;
    
    public Variable left, right;
    public double gap;
    public boolean equality;

    public Constraint(Variable left, Variable right, double gap, boolean equality) {
        this.left = left;
        this.right = right;
        this.gap = gap;
        this.equality = equality;
    }

    double slack() {
        return unsatisfiable ? Double.MAX_VALUE
            : right.scale * right.position() - gap
              - left.scale * left.position();
    }
    
}
package org.cwi.examine.internal.layout.dwyer.vpsc;

import java.util.ArrayList;
import java.util.List;

public class Variable {
    public double offset = 0;
    public Block block;
    public List<Constraint> cIn, cOut;
    
    public double desiredPosition, weight, scale;

    public Variable(double desiredPosition, double weight, double scale) {
        this.cIn = new ArrayList<Constraint>();
        this.cOut = new ArrayList<Constraint>();
        this.desiredPosition = desiredPosition;
        this.weight = weight;
        this.scale = scale;
    }

    public double dfdv() {
        return 2.0 * weight * (position() - desiredPosition);
    }

    public double position() {
        return (block.ps.scale * block.posn + offset) / scale;
    }

    // visit neighbours by active constraints within the same block
    public void visitNeighbours(Variable prev, VisitNeighbour f) {
        for(Constraint c: cOut) if(c.active && prev != c.right) f.apply(c, c.right);
        for(Constraint c: cIn) if(c.active && prev != c.left) f.apply(c, c.left);
    }
    
    public static interface VisitNeighbour {
        
        public void apply(Constraint c, Variable next);
        
    }
    
}
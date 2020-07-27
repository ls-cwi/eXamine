package org.cwi.examine.internal.layout.dwyer.vpsc;

public class PositionStats {
    double AB;
    double AD;
    double A2;
    double scale;

    public PositionStats(double scale) {
        this.AB = 0;
        this.AD = 0;
        this.A2 = 0;
        this.scale = scale;
    }

    public void addVariable(Variable v) {
        double ai = scale / v.scale;
        double bi = v.offset / v.scale;
        double wi = v.weight;
        AB += wi * ai * bi;
        AD += wi * ai * v.desiredPosition;
        A2 += wi * ai * ai;
    }

    public double getPosn() {
        return (AD - AB) / A2;
    }
    
}
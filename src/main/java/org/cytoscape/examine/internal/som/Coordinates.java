package org.cytoscape.examine.internal.som;



/**
 * Neuron topology coordinates, in array space.
 */
public class Coordinates {
    
    // Array coordinates.
    public final int x, y;
    
    // Hexagonal coordinates.
    public final int xH, yH;
    
    /**
     * Base constructor.
     */
    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
        this.xH = Topology.xArrayToHex(x, y);
        this.yH = Topology.yArrayToHex(x, y);
    }
    
    /**
     * Translate in array space.
     */
    public Coordinates translated(int dX, int dY) {
        return new Coordinates(x + dX, y + dY);
    }
    
    /**
     * Translate in array space.
     */
    public Coordinates translated(Coordinates cs) {
        return new Coordinates(x + cs.x, y + cs.y);
    }
    
    /**
     * Translate in hexagonal space.
     */
    public Coordinates hexagonallyTranslated(int dX, int dY) {
        int xNH = xH + dX;
        int yNH = yH + dY;
        return new Coordinates(Topology.xHexToArray(xNH, yNH),
                               Topology.yHexToArray(xNH, yNH));
    }
    
    /**
     * Translate in hexagonal space.
     */
    public Coordinates hexagonallyTranslated(Coordinates cs) {
        int xNH = xH + cs.x;
        int yNH = yH + cs.y;
        return new Coordinates(Topology.xHexToArray(xNH, yNH),
                               Topology.yHexToArray(xNH, yNH));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + this.x;
        hash = 73 * hash + this.y;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        
        final Coordinates other = (Coordinates) obj;
        if(this.x != other.x) {
            return false;
        }
        if(this.y != other.y) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    
}



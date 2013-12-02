package org.cytoscape.examine.internal.som;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.*;

/**
 * Hexagonal tiling topology.
 */
public class Topology {
    
    // Number of neuron across x- and y-axes.
    public final int xSize, ySize;
    
    // Network diameters.
    public final int minimumDiameter, maximumDiameter;
    
    // Number of neurons.
    public final int size;
    
    // Neuron index to position mapping.
    public final int[] x, y;
    
    // Hexagona(!) neighborhood offsets, per distance to source.
    public final int[][] xNeighborhoodOffsets, yNeighborhoodOffsets;
    
    /**
     * Base constructor.
     */
    public Topology(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.minimumDiameter = min(xSize, ySize);
        this.maximumDiameter = max(xSize, ySize);
        this.size = xSize * ySize;
        
        // Neighborhood offsets.
        this.xNeighborhoodOffsets = new int[maximumDiameter][];
        this.yNeighborhoodOffsets = new int[maximumDiameter][];
        
        // Position mapping by topology.
        this.x = new int[size];
        this.y = new int[size];
        for(int i = 0; i < x.length; i++) {
            x[i] = coordinatesOf(i).x;
            y[i] = coordinatesOf(i).y;
        }
        
        // Construct offset neighborhood (rings mapped by distance).
        List<List<Integer>> xOffsets = new ArrayList<List<Integer>>();
        List<List<Integer>> yOffsets = new ArrayList<List<Integer>>();
        for(int i = 0; i < maximumDiameter; i++) {
            xOffsets.add(new ArrayList<Integer>());
            yOffsets.add(new ArrayList<Integer>());
        }
        
        // Scan hexagonal offsets.
        for(int dX = -maximumDiameter + 1; dX < maximumDiameter; dX++) {
            for(int dY = -maximumDiameter + 1; dY < maximumDiameter; dY++) {
                int distance = (dX < 0 && dY < 0) || (dX > 0 && dY > 0) ?
                                    max(abs(dX), abs(dY)) :
                                    abs(dX) + abs(dY);
                
                if(distance < maximumDiameter) {
                    xOffsets.get(distance).add(dX);
                    yOffsets.get(distance).add(dY);
                }
            }
        }
        
        // Convert to arrays.
        for(int i = 0; i < maximumDiameter; i++) {
            int oS = xOffsets.get(i).size();
            xNeighborhoodOffsets[i] = new int[oS];
            yNeighborhoodOffsets[i] = new int[oS];
            
            for(int j = 0; j < oS; j++) {
                xNeighborhoodOffsets[i][j] = xOffsets.get(i).get(j);
                yNeighborhoodOffsets[i][j] = yOffsets.get(i).get(j);
            }
        }
    }
    
    /**
     * Array coordinates of given neuron.
     */
    public Coordinates coordinatesOf(int neuron) {
        return new Coordinates(neuron % xSize, neuron / xSize);
    }
    
    /**
     * Neuron at given array coordinates.
     */
    public int neuronAt(Coordinates coords) {
        return coords.x + coords.y * xSize;
    }
    
    /**
     * Neuron at given array coordinates.
     */
    public int neuronAt(int x, int y) {
        return x + y * xSize;
    }
    
    // --- Begin hexagon coordinate conversions. ---
    
    public static int floor2(int v) {
        return v >= 0 ? v >> 1 : (v - 1) / 2;
    }
    
    public static int ceil2(int v) {
        return v >= 0 ? (v + 1) >> 1 : v / 2;
    }
    
    public static int xArrayToHex(int x, int y) {
        return x - floor2(y);
    }
            
    public static int yArrayToHex(int x, int y) {
        return x + ceil2(y);
    }
    
    public static int xHexToArray(int x, int y) {
        return floor2(x + y);
    }
    
    public static int yHexToArray(int x, int y) {
        return y - x;
    }
    
    // --- End hexagon coordinate conversions. ---
    
}

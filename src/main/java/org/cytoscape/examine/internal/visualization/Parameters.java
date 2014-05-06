package org.cytoscape.examine.internal.visualization;

import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import org.cytoscape.examine.internal.signal.gui.VariableGroup;
import org.cytoscape.examine.internal.signal.Variable;

/**
 * General visualization parameters.
 */
public class Parameters {
    
    // Constants.
    public static final int LABEL_MAX_CHARACTERS = 20;
    public static final int LABEL_MAX_LINES = 2;
    public static final double LABEL_PADDING = 4;
    public static final double LABEL_DOUBLE_PADDING = 2 * LABEL_PADDING;
    public static final double LABEL_ROUNDING = 16;
    public static final double LABEL_MARKER_RADIUS = 6;
    public static final double LABEL_BAR_HEIGHT = 30;
    public static final double SCORE_MIN_RADIUS = 3;
    public static final double NODE_RADIUS = 30;
            
    // Display margin size in pixels.
    public static final Variable<Double> margin = new Variable<Double>(15.0);
    
    // Basis of always-shown proteins: none, modules intersection, or modules union.
    public enum StaticProteinBasis {
        None,
        Intersection,
        Union;
    }
    
    // --- Begin Self Organizing Map parameters. ---
    
    public static final VariableGroup som = new VariableGroup("SOM");
    
    // Tile to protein (of union network) ratio.
    public static final Variable<Double> somTileRatio =
            som.create("Tile ratio", 1.1, 1.1, 2.0, 4.0, 8.0);
    
    // Maximum tile radius.
    public static final Variable<Double> somMaxTileRadius =
            som.create("Tile radius", 80.0, 30.0, 30.0, 60.0, 90.0);
    
    // Minimum neighborhood size for training.
    public static final Variable<Integer> somNeighborhoodMin =
            som.create("Min. neighborhood", 0, 0, 1, 3, 6, 12);
    
    // Maximum neighborhood size factor (of network min. diameter).
    public static final Variable<Double> somNeighborhoodMax =
            som.create("Max. neighborhood", 1.0, 0.5, 1.0, 2.0, 4.0, 8.0);
    
    // Topology to set encoding technique.
    public enum TopologyEncoding {
        None,
        Interaction,
        Compact,
        Neighborhood;
    }
    
    public static final Variable<TopologyEncoding> somTopologyEncoding =
            som.createEnum("Topology to sets", TopologyEncoding.Interaction);
    
    // --- End Self Organizing Map parameters. ---
    
    // Contour tile membership threshold.
    public static final Variable<Double> emptyMembershipThreshold =
            som.create("Empty tile cut off", 0.75, 0.1, 0.25, 0.5, 0.75, 0.9, 1.0);
    
    // Scene width and height (display minus margins).
    public static double sceneWidth() {
        return sketchWidth() - 2f * margin.get();
    }
    
    public static double sceneHeight() {
        return sketchHeight() - 2f * margin.get();
    }
    
}

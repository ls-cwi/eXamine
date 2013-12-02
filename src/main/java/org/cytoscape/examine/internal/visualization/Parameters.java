package org.cytoscape.examine.internal.visualization;

import static aether.Aether.*;
import aether.gui.VariableGroup;
import aether.signal.Variable;

import org.cytoscape.examine.internal.som.metrics.CosineSimilarity;
import org.cytoscape.examine.internal.som.metrics.DistanceMeasure;
import org.cytoscape.examine.internal.som.metrics.EuclidesMetric;
import org.cytoscape.examine.internal.som.metrics.JaccardIndex;
import org.cytoscape.examine.internal.som.metrics.ManhattanMetric;
import org.cytoscape.examine.internal.som.metrics.SorensenMeasure;
import org.cytoscape.examine.internal.som.metrics.SupremumNorm;
import org.cytoscape.examine.internal.som.metrics.TanimotoDistance;

/**
 * General visualization parameters.
 */
public class Parameters {
    
    // Display margin size in pixels.
    public static final Variable<Float> margin = new Variable<Float>(15f);
    
    // Show expression category as colored borders.
    //public static final VariableGroup misc = new VariableGroup("Misc.");
    //public static final Variable<Boolean> expressionBorders =
    //        misc.createBoolean("Expression outline", false);
    
    
    // --- Begin visual parameters.
    
    //public static final VariableGroup visual = new VariableGroup("Visual");
    
    // Basis of always-shown proteins: none, modules intersection, or modules union.
    public enum StaticProteinBasis {
        None,
        Intersection,
        Union;
    }
    
    //public static final Variable<StaticProteinBasis> visualStaticProteinBasis =
    //        visual.createEnum("Static proteins", StaticProteinBasis.Intersection);
    
    // Maximum number of ranked sets to show per category.
    //public static final Variable<Integer> visualSetsPerCategory =
    //        visual.create("Sets per category", 20, 5, 10, 20, 40);
    
    // --- End visual parameters.
    
    // --- Begin Self Organizing Map parameters. ---
    
    public static final VariableGroup som = new VariableGroup("SOM");
    
    // Tile to protein (of union network) ratio.
    public static final Variable<Float> somTileRatio =
            som.create("Tile ratio", 1.25f, 1.25f, 2f, 4f, 8f);
    
    // Maximum tile radius.
    public static final Variable<Float> somMaxTileRadius =
            som.create("Tile radius", 60f, 30f, 30f, 60f, 90f);
    
    // Distance measure for training.
    private final static DistanceMeasure[] distanceMeasures = new DistanceMeasure[] {
        new ManhattanMetric(),
        new EuclidesMetric(),
        new TanimotoDistance(),
        new JaccardIndex(),
        new SorensenMeasure(),
        new SupremumNorm(),
        new CosineSimilarity()
    };
    
    public static final Variable<DistanceMeasure> somMeasure =
            som.create("Distance", distanceMeasures[5], distanceMeasures);
    
    // Minimum neighborhood size for training.
    public static final Variable<Integer> somNeighborhoodMin =
            som.create("Min. neighborhood", 0, 0, 1, 3, 6, 12);
    
    // Maximum neighborhood size factor (of network min. diameter).
    public static final Variable<Float> somNeighborhoodMax =
            som.create("Max. neighborhood", 1f, 0.5f, 1f, 2f, 4f, 8f);
    
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
    
    
    // --- Begin contour parameters. ---
    
    public static final Variable<Float> emptyMembershipThreshold =
            som.create("Empty tile cut off", 0.75f, 0.1f, 0.25f, 0.5f, 0.75f, 0.9f, 1f);
    
    // --- End contour parameters. ---
    
    
    // --- Begin interaction parameters. ---
    /*private static final VariableGroup interactions = new VariableGroup("Interactions");
    
    public static final Variable<Boolean> edgeVisible =
            interactions.createBoolean("Visible", true);
    
    public static final Variable<Integer> edgePoints =
            interactions.create("Segments", 3, 2, 3, 5, 10, 20);*/
    
    public static final Variable<Boolean> edgeVisible = new Variable<Boolean>(true);
    
    public static final Variable<Integer> edgePoints = new Variable<Integer>(3);
    
    
    // --- End interaction parameters. ---
    
    
    // --- Begin derived parameters. ---
    
    // Scene width and height (display minus margins).
    public static float sceneWidth() {
        return sketchWidth() - 2f * margin.get();
    }
    
    public static float sceneHeight() {
        return sketchHeight() - 2f * margin.get();
    }
    
    // --- End derived parameters. ---
    
}

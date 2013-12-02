package org.cytoscape.examine.internal.visualization.overview;

import static aether.Aether.*;
import static aether.Math.*;
import aether.color.Color;
import com.vividsolutions.jts.geom.Geometry;

import org.cytoscape.examine.internal.Modules;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.visualization.SetRepresentation;
import org.cytoscape.examine.internal.visualization.Util;

import java.awt.BasicStroke;
import java.awt.Stroke;
import processing.core.PVector;

/**
 * Contour representation of a set.
 */
public class SetContour extends SetRepresentation<HSet> {
    
    // Outline stroke weight and color.
    public static final float OUTLINE_WEIGHT = 1.75f;
    public static final PVector OUTLINE_COLOR = Color.grey(0.33f);
    public static final float OUTLINE_HIGHLIGHT_WEIGHT = 3f;
    public static final PVector OUTLINE_HIGHLIGHT_COLOR = Color.grey(0f);
    
    // Body opacity.
    public static final float BODY_OPACITY = 1f;
    
    // Set index.
    public final int index;
    
    // Protein set.
    public final HSet set;
    
    // Body and outline shapes of set.
    public final Geometry bodyShape, outlineShape;
    
    /**
     * Base constructor.
     */
    public SetContour(HSet set,
                             int index,
                             Geometry bodyShape,
                             Geometry outlineShape) {
        super(set);
        
        this.set = set;
        this.index = index;
        this.bodyShape = bodyShape;
        this.outlineShape = outlineShape;
    }
    
    /**
     * Color of the set.
     */
    private PVector color() {
        PVector color = Modules.visualization.setColors.color(set);
        return color == null ? Color.grey(0.5f) : color;
    }

    /**
     * Draw body.
     */
    @Override
    public void draw() {
        boolean highlight = highlight();
        
        picking();
        
        noStroke();
        fill(color(), BODY_OPACITY);
        noTransition();
        Util.drawGeometry(bodyShape, false);
        transition();
        
        // Solid outline in back.
        noFill();
        stroke(highlight ? OUTLINE_HIGHLIGHT_COLOR : OUTLINE_COLOR);
        strokeWeight(highlight ? OUTLINE_HIGHLIGHT_WEIGHT : OUTLINE_WEIGHT);
        noTransition();
        Util.drawGeometry(outlineShape, true);
        transition();
    }
    
    /**
     * Draw outline.
     */
    public void drawOutline() {
        boolean highlight = highlight();
        
        noFill();
        stroke(highlight ? OUTLINE_HIGHLIGHT_COLOR : OUTLINE_COLOR);
        
        // Dithered outline in front (hack to Graphics2D).
        Stroke oldStroke = g().getStroke();
        BasicStroke pen = new BasicStroke(
                (highlight ? OUTLINE_HIGHLIGHT_WEIGHT : OUTLINE_WEIGHT) - 0.25f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND,
                4.0f,
                new float[]{3f, 3f},
                0.0f);
        g().setStroke(pen);
        Util.drawGeometry(outlineShape, true);
        
        g().setStroke(oldStroke);
    }

    @Override
    public PVector dimensions() {
        return v();
    }
    
}

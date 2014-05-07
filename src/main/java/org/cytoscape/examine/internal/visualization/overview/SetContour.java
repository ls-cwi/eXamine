package org.cytoscape.examine.internal.visualization.overview;

import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import static org.cytoscape.examine.internal.graphics.Math.*;

import com.vividsolutions.jts.geom.Geometry;

import org.cytoscape.examine.internal.Modules;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.visualization.SetRepresentation;
import org.cytoscape.examine.internal.visualization.Util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;

import org.cytoscape.examine.internal.graphics.Colors;
import org.cytoscape.examine.internal.graphics.PVector;
import org.cytoscape.examine.internal.graphics.StaticGraphics;

/**
 * Contour representation of a set.
 */
public class SetContour extends SetRepresentation<HSet> {
    
    // Outline color weight and color.
    public static final double OUTLINE_WEIGHT = 1.75f;
    public static final Color OUTLINE_COLOR = Colors.grey(0.33f);
    public static final double OUTLINE_HIGHLIGHT_WEIGHT = 3f;
    public static final Color OUTLINE_HIGHLIGHT_COLOR = Colors.grey(0f);
    
    // Body opacity.
    public static final double BODY_OPACITY = 1f;
    
    // Set index.
    public final int index;
    
    // Protein set.
    public final HSet set;
    
    // Body and outline shapes of set.
    public final Geometry body, outline;
    public final Shape bodyShape, outlineShape;
    
    /**
     * Base constructor.
     */
    public SetContour(HSet set, int index, Geometry body, Geometry outline) {
        super(set);
        
        this.set = set;
        this.index = index;
        this.body = body;
        this.bodyShape = Util.geometryToShape(body);
        this.outline = outline;
        this.outlineShape = Util.geometryToShape(outline);
    }
    
    /**
     * Color of the set.
     */
    private Color bandColor() {
        Color color = Modules.visualization.setColors.color(set);
        return color == null ? Colors.grey(0.5f) : color;
    }

    /**
     * Draw body.
     */
    @Override
    public void draw() {
        boolean highlight = highlight();
        
        picking();
        
        color(bandColor(), BODY_OPACITY);
        fill(bodyShape);
        
        // Solid outline in back.
        color(highlight ? OUTLINE_HIGHLIGHT_COLOR : OUTLINE_COLOR);
        strokeWeight(highlight ? OUTLINE_HIGHLIGHT_WEIGHT : OUTLINE_WEIGHT);
        StaticGraphics.draw(outlineShape);
    }
    
    /**
     * Draw outline.
     */
    public void drawOutline() {
        boolean highlight = highlight();
        
        color(highlight ? OUTLINE_HIGHLIGHT_COLOR : OUTLINE_COLOR);
        
        // Dithered outline in front (hack to Graphics2D).
        pushStyle();
        BasicStroke pen = new BasicStroke(
                (float) (highlight ? OUTLINE_HIGHLIGHT_WEIGHT : OUTLINE_WEIGHT) - .25f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND,
                4f,
                new float[]{3f, 3f},
                0f);
        stroke(pen);
        
        StaticGraphics.draw(outlineShape);
        
        popStyle();
    }

    @Override
    public PVector dimensions() {
        return v();
    }
    
}

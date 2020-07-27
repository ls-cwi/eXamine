package org.cwi.examine.internal.visualization.overview;

import com.sun.javafx.collections.ObservableSetWrapper;
import com.vividsolutions.jts.geom.Geometry;

import org.cwi.examine.internal.graphics.PVector;
import org.cwi.examine.internal.visualization.Visualization;
import org.cwi.examine.internal.data.HAnnotation;
import org.cwi.examine.internal.visualization.SetRepresentation;
import org.cwi.examine.internal.visualization.Util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.util.HashSet;
import java.util.Set;
import org.cwi.examine.internal.data.HNode;

import org.cwi.examine.internal.graphics.Colors;
import org.cwi.examine.internal.graphics.StaticGraphics;

import static org.cwi.examine.internal.graphics.StaticGraphics.*;

// Contour representation of a set.
public class SetContour extends SetRepresentation {
    public static final double OUTLINE_WEIGHT = 1.75f;
    public static final Color OUTLINE_COLOR = Colors.grey(0.33f);
    public static final double OUTLINE_HIGHLIGHT_WEIGHT = 3f;
    public static final Color OUTLINE_HIGHLIGHT_COLOR = Colors.grey(0f);
    public static final double BODY_OPACITY = 1f;

    // Set index.
    public final int index;
    
    // Protein set.
    public final HAnnotation set;
    
    // Body and outline shapes of set.
    public final Geometry body, outline;
    public final Shape bodyShape, outlineShape;
    
    public SetContour(final Visualization visualization, HAnnotation set, int index, Geometry body, Geometry outline) {
        super(visualization, set);

        this.set = set;
        this.index = index;
        this.body = body;
        this.bodyShape = Util.geometryToShape(body, 0); //0.0001);
        this.outline = outline;
        this.outlineShape = Util.geometryToShape(outline, 0); // 0.0001);
    }
    
    // Color of the set.
    private Color bandColor() {
        Color color = visualization.setColors.color(set);
        return color == null ? Colors.grey(0.5f) : color;
    }

    // Draw body.
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
    
    // Draw outline.
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
        return PVector.v();
    }
    
    @Override
    public void beginHovered() {
        // Highlight proteins term intersection.
        Set<HNode> hP = new HashSet<>();
        hP.addAll(element.elements);
        visualization.model.highlightedProteins.set(new ObservableSetWrapper<>(hP));
        
        // Highlight annotation annotations that contain all elements of this set.
        Set<HAnnotation> hT = new HashSet<>();
        hT.addAll(element.elements.get(0).annotations);
        for(int i = 1; i < element.elements.size(); i++) {
            hT.retainAll(element.elements.get(i).annotations);
        }
        visualization.model.highlightedSets.set(new ObservableSetWrapper<>(hT));
    }

    @Override
    public void endHovered() {
        visualization.model.highlightedProteins.clear();
        visualization.model.highlightedSets.clear();
    }

    @Override
    public int hashCode() {
        return Object.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return Object.class.equals(obj);
    }
}

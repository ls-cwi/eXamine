package org.cytoscape.examine.internal.visualization.overview;

import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.graphics.PVector;
import org.cytoscape.examine.internal.graphics.StaticGraphics;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import org.cytoscape.examine.internal.layout.Layout;
import org.cytoscape.examine.internal.model.Model;
import org.cytoscape.examine.internal.visualization.SetRepresentation;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.jgrapht.graph.DefaultEdge;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cytoscape.examine.internal.graphics.StaticGraphics.color;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.fill;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.mouseEvent;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.picking;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.strokeWeight;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.text;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.translate;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.containmentColor;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.textContainedColor;
import static org.cytoscape.examine.internal.visualization.Parameters.NODE_OUTLINE;

// Node representation.
public class NodeRepresentation extends Representation<HNode> {

    private final Model model;
    
    public NodeRepresentation(Model model, HNode element) {
        super(element);

        this.model = model;
    }

    @Override
    public PVector dimensions() {
        return PVector.v();
    }

    @Override
    public void draw() {
        color(Color.BLACK);
        translate(topLeft);
        
        // Get label bounds, but also sets label font.
        PVector bounds = Layout.labelDimensions(element, true);
        Shape shape = shape(bounds);
        translate(-0.5 * bounds.x, -0.5 * bounds.y);
        
        // Background rectangle.
        color(highlight() ? containmentColor :
                            (Color) styleValue(BasicVisualLexicon.NODE_FILL_COLOR));
        fill(shape);
        //fillRect(0, 0, bounds.x, bounds.y, bounds.y);
        
        // Foreground outline with color coding.
        color((Color) styleValue(BasicVisualLexicon.NODE_BORDER_PAINT));
        strokeWeight(styleValue(BasicVisualLexicon.NODE_BORDER_WIDTH));
        StaticGraphics.draw(shape);
        //drawRect(0, 0, bounds.x, bounds.y, bounds.y);
        
        picking();
        color(highlight() ? textContainedColor :
                            (Color) styleValue(BasicVisualLexicon.NODE_LABEL_COLOR));
        text(element.toString(), 0.5 * (bounds.y + NODE_OUTLINE) - 3,
                                 bounds.y - NODE_OUTLINE - 3);
    }
    
    private Shape shape(PVector bounds) {
        Shape result;
        
        NodeShape cyShape = styleValue(BasicVisualLexicon.NODE_SHAPE);
        
        // Hexagon.
        if(cyShape.equals(NodeShapeVisualProperty.HEXAGON)) {
            double r = 0.5 * bounds.y;
            double hr = 0.5 * r;
            
            Path2D path = new Path2D.Double();
            path.moveTo(0, r);
            path.lineTo(hr, 0);
            path.lineTo(bounds.x - hr, 0);
            path.lineTo(bounds.x, r);
            path.lineTo(bounds.x - hr, bounds.y);
            path.lineTo(hr, bounds.y);
            path.closePath();
            
            result = path;
        }
        // Octagon.
        else if(cyShape.equals(NodeShapeVisualProperty.OCTAGON)) {
            double hhr = 0.3 * bounds.y;
            
            Path2D path = new Path2D.Double();
            path.moveTo(0, hhr);
            path.lineTo(hhr, 0);
            path.lineTo(bounds.x - hhr, 0);
            path.lineTo(bounds.x, hhr);
            path.lineTo(bounds.x, bounds.y - hhr);
            path.lineTo(bounds.x - hhr, bounds.y);
            path.lineTo(hhr, bounds.y);
            path.lineTo(0, bounds.y - hhr);
            path.closePath();
            
            result = path;
        }
        // Rounded rectangle.
        else {
            result = new RoundRectangle2D.Double(
                        0, 0,
                        bounds.x, bounds.y,
                        bounds.y, bounds.y);
        }
        
        return result;
    }
    
    private <V> V styleValue(VisualProperty<V> property) {
        return model.styleValue(property, element.cyRow);
    }

    @Override
    public void beginHovered() {
        // Highlight protein, its adjacent interactions, and its member terms.
        Set<HNode> hP = new HashSet<HNode>();
        hP.add(element);
        model.highlightedProteins.set(hP);
        
        // Highlight interactions.
        Set<DefaultEdge> hI = new HashSet<DefaultEdge>();
        Set<DefaultEdge> edges = model.activeNetwork.get().graph.edgesOf(element);
        hI.addAll(edges);
        model.highlightedInteractions.set(hI);
        
        // Highlight member terms.
        Set<HSet> hT = new HashSet<HSet>();
        for(HSet set: element.sets) {
            hT.add(set);
        }
        model.highlightedSets.set(hT);
    }

    @Override
    public void endHovered() {
        model.highlightedProteins.clear();
        model.highlightedInteractions.clear();
        model.highlightedSets.clear();
    }
    
    private boolean highlight() {
        return model.highlightedProteins.get().contains(element);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Open website(s) on ctrl click.
        if(mouseEvent().isControlDown()) {
            if(element.url != null && element.url.trim().length() > 0) {
                try {
                    Desktop.getDesktop().browse(URI.create(element.url));
                } catch(IOException ex) {
                    Logger.getLogger(SetRepresentation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            model.selection.select(element);
        }
    }
}
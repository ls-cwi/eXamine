package org.cytoscape.examine.internal.visualization.overview;

import java.awt.Color;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import static org.cytoscape.examine.internal.graphics.Math.*;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.*;
import static org.cytoscape.examine.internal.visualization.Parameters.*;
import org.cytoscape.examine.internal.graphics.draw.Representation;

import org.cytoscape.examine.internal.ViewerAction;
import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.visualization.SetRepresentation;

import static org.cytoscape.examine.internal.Modules.*;

import java.util.HashSet;
import java.util.Set;
import java.awt.Desktop;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cytoscape.examine.internal.graphics.Colors;
import org.cytoscape.examine.internal.graphics.PVector;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.jgrapht.graph.DefaultEdge;

/**
 * Protein representation.
 */
public class NodeRepresentation extends Representation<HNode> {
    
    // Auxiliary protein color.
    public final Color AUXILIARY_COLOR = Colors.grey(0.75f);
    
    // Represented feature vector.
    public final double[] vector;
    
    private double textSpan;

    /**
     * Base constructor.
     */
    public NodeRepresentation(HNode element, double[] vector) {
        super(element);
        
        this.vector = vector;
        this.textSpan = -1;
    }

    @Override
    public PVector dimensions() {
        return v();
    }

    @Override
    public void draw() {
        color(Color.BLACK);
        translate(topLeft);
        
        if(textSpan < 0) {
            textSpan = textWidth(element.toString());
        }
        
        translate(-textSpan / 2f, -textHeight() / 2f);
        
        VisualMappingFunction<?, Paint> mappingFunction =
            ViewerAction.visualMappingManager
                        .getCurrentVisualStyle()
                        .getVisualMappingFunction(BasicVisualLexicon.NODE_FILL_COLOR);
        
        // Foreground outline with color coding.
        Color cytoColor = mappingFunction != null ?
            (Color) mappingFunction.getMappedValue(element.cyRow) : Color.WHITE;
        
        // Background rectangle.
        if(highlight()) {
            color(containmentColor.get());
        } else {
            color(cytoColor.brighter());
        }
        
        double radius = NODE_RADIUS; //0.5 * SOMOverview.tileRadius;
        fillEllipse(textSpan / 2f, textHeight() / 2f, radius, radius);
        //fillRect(-LABEL_PADDING, -LABEL_PADDING,
        //         textSpan + LABEL_DOUBLE_PADDING, textHeight() + LABEL_DOUBLE_PADDING, 
        //         LABEL_ROUNDING);
        
        color(cytoColor);
        strokeWeight(4f);
        
        //drawRect(-LABEL_PADDING, -LABEL_PADDING,
        //         textSpan + LABEL_DOUBLE_PADDING, textHeight() + LABEL_DOUBLE_PADDING,
        //         LABEL_ROUNDING);
        
        drawEllipse(textSpan / 2f, textHeight() / 2f, radius, radius);
        
        // Label; small font face.
        //translate(0f, -textAscent() / 2f);
        
        textFont(labelFont.get());
        color(highlight() ? textContainedColor.get() : textColor.get());
        
        picking();
        text(element.toString());
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
            try {
                String url = null;
                if(element.url != null && element.url.trim().length() > 0) {
                    url = element.url;
                }
                
                if(url != null) {
                    Desktop.getDesktop().browse(URI.create(url));
                }
            } catch(IOException ex) {
                Logger.getLogger(SetRepresentation.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            model.selection.select(element);
        }
    }
    
}
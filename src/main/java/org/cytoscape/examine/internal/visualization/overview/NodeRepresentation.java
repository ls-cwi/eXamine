package org.cytoscape.examine.internal.visualization.overview;

import aether.Aether;
import static aether.Aether.*;
import static aether.Math.*;
import aether.color.Color;
import static aether.draw.Parameters.*;
import aether.draw.Representation;

import org.cytoscape.examine.internal.Constants;
import org.cytoscape.examine.internal.ViewerAction;
import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.visualization.SetRepresentation;

import static org.cytoscape.examine.internal.Modules.*;
import static org.cytoscape.examine.internal.visualization.Parameters.*;

import java.util.HashSet;
import java.util.Set;
import processing.core.PVector;
import java.awt.Desktop;
import java.awt.Paint;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;

/**
 * Protein representation.
 */
public class NodeRepresentation extends Representation<HNode> {
    
    // Auxiliary protein color.
    public final PVector AUXILIARY_COLOR = Color.grey(0.75f);
    
    // Represented feature vector.
    public final float[] vector;
    
    private float textSpan;

    /**
     * Base constructor.
     */
    public NodeRepresentation(HNode element, float[] vector) {
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
        fill(0f);
        translate(topLeft);
        
        if(textSpan < 0) {
            textSpan = textWidth(element.toString());
        }
        
        translate(-textSpan / 2f, -textAscent() / 2f);
        
        VisualMappingFunction<?, Paint> mappingFuction = ViewerAction
        								.visualMappingManager
        								.getCurrentVisualStyle()
        								.getVisualMappingFunction(BasicVisualLexicon.NODE_FILL_COLOR);
        
        // Rounded rectangle backdrop.
        java.awt.Color cytoColor = null;
        if (mappingFuction != null) {
            cytoColor = (java.awt.Color) mappingFuction.getMappedValue(element.cyRow);        	
        }

        PVector nodeColor = cytoColor == null ?
                Color.white :
                Color.rgb(cytoColor.getRed(), cytoColor.getGreen(), cytoColor.getBlue());
        stroke(nodeColor);
        strokeWeight(3f);
        
        fill(highlight() ? containmentColor.get() : Color.white);
        rect(-2f, -2f, textSpan + 4f, textAscent() + 4f, 6f);
        
        // Small font face.
        translate(0f, -textAscent() / 2f + 1.5f);
        
        textFont(labelFont.get());
        noStroke();
        fill(highlight() ? textContainedColor.get() : textColor.get());
        
        picking();
        text(element.toString());
    }

    @Override
    public void beginHovered() {
        // Highlight protein and its member terms.
        Set<HNode> hP = new HashSet<HNode>();
        hP.add(element);
        model.highlightedProteins.set(hP);
        
        Set<HSet> hT = new HashSet<HSet>();
        for(HSet set: element.sets) {
            hT.add(set);
        }
        model.highlightedSets.set(hT);
    }

    @Override
    public void endHovered() {
        model.highlightedSets.clear();
        model.highlightedProteins.clear();
    }
    
    private boolean highlight() {
        return model.highlightedProteins.get().contains(element);
    }

    @Override
    public void mouseClicked() {
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
package org.cytoscape.examine.internal.visualization.overview;

import static aether.Aether.*;
import static aether.Math.*;
import aether.draw.Representation;
import java.util.HashSet;
import java.util.Set;
import static org.cytoscape.examine.internal.Modules.*;

import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.data.Network;
import org.jgrapht.graph.DefaultEdge;
import processing.core.PVector;

/**
 * Interaction representation.
 */
public class InteractionRepresentation extends Representation<DefaultEdge> {

    // Opacity.
    public static final float OPACITY = 1f;
    
    // Connected proteins.
    public final HNode protein1, protein2;
    
    // X and Y coordinates.
    public final PVector[] cs;

    /**
     * Base constructor.
     */
    public InteractionRepresentation(DefaultEdge element,
                                     PVector[] cs) {
        super(element);
        
        Network network = data.superNetwork.get();
        this.protein1 = network.graph.getEdgeSource(element);
        this.protein2 = network.graph.getEdgeTarget(element);
        
        this.cs = cs;
    }

    @Override
    public PVector dimensions() {
        return v();
    }

    @Override
    public void draw() {
        boolean highlight = model.highlightedInteractions.get().contains(element);
                            //model.highlightedProteins.get().contains(protein1) ||
                            //model.highlightedProteins.get().contains(protein2);
        
        picking();
        noFill();

        // Halo.
        noFill();
        float haloWeight = highlight ? 10f : 8f;
        stroke(1f, highlight ? 1f : OPACITY);
        strokeWeight(haloWeight);
        drawLine();
        
        float endWeight = haloWeight + 2f;
        noStroke();
        fill(highlight ? 1f : OPACITY);
        ellipse(cs[0].x, cs[0].y, endWeight, endWeight);
        ellipse(cs[cs.length - 1].x, cs[cs.length - 1].y, endWeight, endWeight);
        
        // Actual edge.
        noFill();
        float edgeWeight = highlight ? 3f : 2f;
        stroke(highlight ? 0f : 0f, highlight ? 1f: OPACITY);
        strokeWeight(edgeWeight);
        drawLine();
        
        endWeight = 2f * edgeWeight;
        noStroke();
        fill(highlight ? 0f : 0.25f);
        ellipse(cs[0].x, cs[0].y, endWeight, endWeight);
        ellipse(cs[cs.length - 1].x, cs[cs.length - 1].y, endWeight, endWeight);
    }
    
    private void drawLine() {
        /*beginShape();
        for(int i = 0; i < cs.length; i++) {
            //float alpha = (float) i / (float) (cs.length - 1);
            vertex(cs[i].x, cs[i].y);
        }
        endShape();*/
        
        beginShape();
        curveVertex(cs[0]);
        for(int i = 0; i < cs.length; i++) {
            curveVertex(cs[i]);
        }
        curveVertex(cs[cs.length - 1]);
        endShape();
    }

    @Override
    public void beginHovered() {
        // Highlight proteins term intersection.
        Set<HNode> hP = new HashSet<HNode>();
        hP.add(protein1);
        hP.add(protein2);
        model.highlightedProteins.set(hP);
        
        // Highlight interactions.
        Set<DefaultEdge> hI = new HashSet<DefaultEdge>();
        hI.add(element);
        model.highlightedInteractions.set(hI);
        
        // Intersect annotation sets.
        Set<HSet> hT = new HashSet<HSet>();
        hT.addAll(protein1.sets);
        hT.retainAll(protein2.sets);
        model.highlightedSets.set(hT);
    }

    @Override
    public void endHovered() {
        model.highlightedProteins.clear();
        model.highlightedInteractions.clear();
        model.highlightedSets.clear();
    }

}

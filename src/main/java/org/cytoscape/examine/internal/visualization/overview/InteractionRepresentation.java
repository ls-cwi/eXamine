package org.cytoscape.examine.internal.visualization.overview;

import java.awt.Color;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import static org.cytoscape.examine.internal.graphics.Math.*;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import java.util.HashSet;
import java.util.Set;
import static org.cytoscape.examine.internal.Modules.*;

import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.data.Network;
import org.cytoscape.examine.internal.graphics.PVector;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.*;
import org.jgrapht.graph.DefaultEdge;

/**
 * Interaction representation.
 */
public class InteractionRepresentation extends Representation<DefaultEdge> {
    
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
        
        picking();

        // Halo.
        double haloWeight = highlight ? 6f : 4f;
        color(backgroundColor.get());
        strokeWeight(haloWeight);
        drawLine();
        
        fillEllipse(cs[0].x, cs[0].y, haloWeight, haloWeight);
        fillEllipse(cs[cs.length - 1].x, cs[cs.length - 1].y, haloWeight, haloWeight);
        
        // Actual edge.
        double edgeWeight = highlight ? 5f : 3f;
        color(highlight ? Color.BLACK: textColor.get());
        strokeWeight(edgeWeight);
        drawLine();
        
        color(highlight ? Color.BLACK: textColor.get());
        fillEllipse(cs[0].x, cs[0].y, edgeWeight, edgeWeight);
        fillEllipse(cs[cs.length - 1].x, cs[cs.length - 1].y, edgeWeight, edgeWeight);
    }
    
    private void drawLine() {
        drawCurve(cs[0], cs[1], cs[2]);
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

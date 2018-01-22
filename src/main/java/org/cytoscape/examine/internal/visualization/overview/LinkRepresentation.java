package org.cytoscape.examine.internal.visualization.overview;

import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.graphics.AnimatedGraphics;
import org.cytoscape.examine.internal.graphics.PVector;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import org.cytoscape.examine.internal.model.Model;
import org.jgrapht.graph.DefaultEdge;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static org.cytoscape.examine.internal.graphics.draw.Constants.BACKGROUND_COLOR;
import static org.cytoscape.examine.internal.graphics.draw.Constants.TEXT_COLOR;
import static org.cytoscape.examine.internal.visualization.Constants.LINK_WIDTH;

// Link representation.
public class LinkRepresentation extends Representation<LinkRepresentation.Link> {

    private final Model model;
    public final DefaultEdge edge;  // Underlying edge.
    public final PVector[] cs;

    public LinkRepresentation(
            Model model,
            DefaultEdge edge,
            HNode node1,
            HNode node2,
            PVector[] cs) {
        super(new Link(node1, node2));

        this.model = model;
        this.edge = edge;
        this.cs = cs;
    }

    @Override
    public PVector dimensions(AnimatedGraphics g) {
        return PVector.v();
    }

    @Override
    public void draw(AnimatedGraphics g) {
        boolean highlight = model.highlightedInteractions.get().contains(edge);
        
        g.picking();

        // Halo.
        double haloWeight = highlight ? LINK_WIDTH + 4f : LINK_WIDTH + 2f;
        g.color(BACKGROUND_COLOR);
        g.strokeWeight(haloWeight);
        drawLink(g);
        
        // Actual edge.
        double edgeWeight = highlight ? LINK_WIDTH + 2f : LINK_WIDTH;
        g.color(highlight ? Color.BLACK: TEXT_COLOR);
        g.strokeWeight(edgeWeight);
        drawLink(g);
    }
    
    private void drawLink(AnimatedGraphics g) {
        g.circleArc(cs[0], cs[1], cs[2]);
        //drawLineString(ls);
        //drawLine(cs[0], cs[1]);
        //drawLine(cs[1], cs[2]);
    }

    @Override
    public void beginHovered() {
        // Highlight proteins term intersection.
        Set<HNode> hP = new HashSet<HNode>();
        hP.add(element.node1);
        hP.add(element.node2);
        model.highlightedProteins.set(hP);
        
        // Highlight interactions.
        Set<DefaultEdge> hI = new HashSet<DefaultEdge>();
        hI.add(edge);
        model.highlightedInteractions.set(hI);
        
        // Intersect annotation sets.
        Set<HSet> hT = new HashSet<HSet>();
        hT.addAll(element.node1.sets);
        hT.retainAll(element.node2.sets);
        model.highlightedSets.set(hT);
    }

    @Override
    public void endHovered() {
        model.highlightedProteins.clear();
        model.highlightedInteractions.clear();
        model.highlightedSets.clear();
    }
    
    
    public static class Link {
        public HNode node1, node2;

        public Link(HNode node1, HNode node2) {
            this.node1 = node1;
            this.node2 = node2;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + (this.node1 != null ? this.node1.hashCode() : 0);
            hash = 29 * hash + (this.node2 != null ? this.node2.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Link other = (Link) obj;
            if (this.node1 != other.node1 && (this.node1 == null || !this.node1.equals(other.node1))) {
                return false;
            }
            if (this.node2 != other.node2 && (this.node2 == null || !this.node2.equals(other.node2))) {
                return false;
            }
            return true;
        }
    }
}

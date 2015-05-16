package org.cytoscape.examine.internal.visualization.overview;

import java.awt.Color;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import java.util.HashSet;
import java.util.Set;
import static org.cytoscape.examine.internal.Modules.*;

import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.graphics.PVector;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.*;
import static org.cytoscape.examine.internal.visualization.Parameters.*;
import org.jgrapht.graph.DefaultEdge;

// Link representation.
public class LinkRepresentation extends Representation<LinkRepresentation.Link> {
    public final DefaultEdge edge;  // Underlying edge.
    public final PVector[] cs;
    //public final LineString ls;     // Curve coordinates.

    public LinkRepresentation(DefaultEdge edge,
                              HNode node1,
                              HNode node2,
                              PVector[] cs) {
        super(new Link(node1, node2));
        this.edge = edge;
        this.cs = cs;
        //this.ls = Util.circlePiece(cs[0], cs[1], cs[2], LINK_SEGMENTS);
    }

    @Override
    public PVector dimensions() {
        return PVector.v();
    }

    @Override
    public void draw() {
        boolean highlight = model.highlightedInteractions.get().contains(edge);
        
        picking();

        // Halo.
        double haloWeight = highlight ? LINK_WIDTH + 4f : LINK_WIDTH + 2f;
        color(backgroundColor);
        strokeWeight(haloWeight);
        drawLink();
        
        // Actual edge.
        double edgeWeight = highlight ? LINK_WIDTH + 2f : LINK_WIDTH;
        color(highlight ? Color.BLACK: textColor);
        strokeWeight(edgeWeight);
        drawLink();
    }
    
    private void drawLink() {
        circleArc(cs[0], cs[1], cs[2]);
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

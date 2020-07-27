package org.cwi.examine.internal.visualization.overview;

import java.awt.Color;

import com.sun.javafx.collections.ObservableSetWrapper;
import org.cwi.examine.internal.graphics.PVector;
import org.cwi.examine.internal.graphics.draw.Parameters;
import org.cwi.examine.internal.graphics.draw.Representation;
import org.cwi.examine.internal.visualization.Visualization;

import org.cwi.examine.internal.data.HNode;
import org.cwi.examine.internal.data.HAnnotation;
import org.cwi.examine.internal.visualization.SetRepresentation;
import org.cwi.examine.internal.molepan.*;

import java.util.HashSet;
import java.util.Set;
import java.awt.Desktop;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cwi.examine.internal.graphics.StaticGraphics;
import org.cwi.examine.internal.layout.Layout;
import org.jgrapht.graph.DefaultEdge;

import org.cwi.examine.internal.Option;

import static org.cwi.examine.internal.graphics.StaticGraphics.*;

// Node representation.
public class NodeRepresentation extends Representation<HNode> {

    private final Visualization visualization;
    
    
    
    public NodeRepresentation(final Visualization visualization, HNode element) {
        super(element);

        this.visualization = visualization;
        
        
    }

    @Override
    public PVector dimensions() {
        return PVector.v();
    }

    @Override
    public void draw() {
    
    	  boolean scel =  Option.getScel();
    	ConvertToAtom cta = new ConvertToAtom();
        color(Color.WHITE);
        translate(topLeft);
        
        // Get label bounds, but also annotations label font.
        PVector bounds = Layout.labelDimensions(element, true);
        Shape shape = shape(bounds);
        
        if(!element.toString().contains("C"))   
        translate(-0.5 * bounds.x, -0.5 * bounds.y);
        
        
       // color(highlight() ? Parameters.containmentColor : Color.BLACK);
       
    
       
       if(element.toString().contains("C") )  	{							//OPTION
       
       if(scel ==false)
       	 translate(-0.5 * bounds.x, -0.5 * bounds.y);
       	 
       	  if (scel ==true)
        translate(-1110.5 * bounds.x, -1110.5 * bounds.y);
         
         
         }
         
         
         
         
        // Background rectangle.
        if(!element.toString().contains("H"))
        color(highlight() ? Parameters.containmentColor : Color.WHITE);
        
        
        
        
        
        
        if(element.toString().contains("H"))	{									//OPTION
         if(scel ==false)
           color(highlight() ? Parameters.containmentColor : Color.WHITE);
         //color(highlight() ? Parameters.containmentColor : Color.BLACK);
         
         if (scel ==true)
          translate(-1110.5 * bounds.x, -1110.5 * bounds.y);
         
                            //(Color) styleValue(BasicVisualLexicon.NODE_FILL_COLOR));
                            
                            
                   }         
                            
                            
        fill(shape);
        
        // Foreground outline with color coding.
         if(scel ==false)
        color(Color.WHITE); //(Color) styleValue(BasicVisualLexicon.NODE_BORDER_PAINT));
        strokeWeight(0.5);    //styleValue(BasicVisualLexicon.NODE_BORDER_WIDTH));
        StaticGraphics.draw(shape);
        
        picking();
        
        //Kalottenmodell
        
        color(highlight() ? Parameters.textContainedColor : Color.BLACK);
        
        if(element.toString().contains("O"))
        color(highlight() ? Parameters.textContainedColor : Color.RED);
        
        if(element.toString().contains("H"))
         color(highlight() ? Parameters.textContainedColor : Color.BLACK);
      // color(highlight() ? Parameters.textContainedColor : Color.WHITE);
        
        if(element.toString().contains("N"))
        color(highlight() ? Parameters.textContainedColor : Color.BLUE);
        
        
       //(Color) styleValue(BasicVisualLexicon.NODE_LABEL_COLOR));
       
       
        if(!element.toString().contains("C") || scel == false )										//OPTION
       // color(highlight() ? Parameters.textContainedColor : Color.RED);
       
       
        text(cta.convert_to_atom(element.toString() ) , 0.5 * (bounds.y + org.cwi.examine.internal.visualization.Parameters.NODE_OUTLINE) - 3, bounds.y - org.cwi.examine.internal.visualization.Parameters.NODE_OUTLINE - 3);
   
       // text(element.toString() , 0.5 * (bounds.y + org.cwi.examine.internal.visualization.Parameters.NODE_OUTLINE) - 3, bounds.y - org.cwi.examine.internal.visualization.Parameters.NODE_OUTLINE - 3);
   
   	//
   
   
   
    }
    
    private Shape shape(PVector bounds) {
        Shape result;

        String cyShape = "Rounded";
        //NodeShape cyShape = styleValue(BasicVisualLexicon.NODE_SHAPE);
        
        // Hexagon.
        if(cyShape.equals("Hexagon")) {
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
        else if(cyShape.equals("Octagon")) {
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
    
//    private <V> V styleValue(VisualProperty<V> property) {
//        return Model.styleValue(property, element.cyRow);
//    }

    @Override
    public void beginHovered() {
        // Highlight protein, its adjacent interactions, and its member terms.
        Set<HNode> hP = new HashSet<>();
        hP.add(element);
        visualization.model.highlightedProteins.set(new ObservableSetWrapper<>(hP));
        
        // Highlight interactions.
        Set<DefaultEdge> hI = new HashSet<>();
        Set<DefaultEdge> edges = visualization.model.activeNetwork.get().graph.edgesOf(element);
        hI.addAll(edges);
        visualization.model.highlightedInteractions.set(new ObservableSetWrapper<>(hI));
        
        // Highlight member terms.
        Set<HAnnotation> hT = new HashSet<>();
        hT.addAll(element.annotations);
        visualization.model.highlightedSets.set(new ObservableSetWrapper<>(hT));
    }

    @Override
    public void endHovered() {
        visualization.model.highlightedProteins.clear();
        visualization.model.highlightedInteractions.clear();
        visualization.model.highlightedSets.clear();
    }
    
    private boolean highlight() {
        return visualization.model.highlightedProteins.get().contains(element);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Open website(s) on ctrl click.
        if(mouseEvent().isControlDown()) {
            if(element.url != null && element.url.trim().length() > 0) {
                try {
                    Desktop.getDesktop().browse(URI.create(element.url));
                    System.out.println("test");
                } catch(IOException ex) {
                    Logger.getLogger(SetRepresentation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            visualization.model.selection.select(element);
        }
    }
}
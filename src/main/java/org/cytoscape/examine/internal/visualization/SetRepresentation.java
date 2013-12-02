package org.cytoscape.examine.internal.visualization;

import static aether.Aether.*;
import aether.draw.PositionedSnippet;
import static org.cytoscape.examine.internal.Modules.*;

import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ProteinSet representation.
 */
public abstract class SetRepresentation<E extends HSet> extends PositionedSnippet { //extends Representation<E> {

    public final E element;
    
    /**
     * Base constructor.
     */
    public SetRepresentation(E element) {
        //super(element);
        
        this.element = element;
    }
    
    public boolean highlight() {
        return model.highlightedSets.get().contains(element);
    }

    @Override
    public void beginHovered() {
        // Highlight term and its member proteins.
        Set<HSet> hT = new HashSet<HSet>();
        hT.add(element);
        model.highlightedSets.set(hT);
        
        Set<HNode> hP = new HashSet<HNode>();
        hP.addAll(element.elements);
        model.highlightedProteins.set(hP);
    }

    @Override
    public void endHovered() {
        model.highlightedSets.clear();
        model.highlightedProteins.clear();
    }  

    @Override
    public void mouseWheel(int rotation) {
        // Adjust weight if set is selected.
        if(model.selection.activeSetMap.keySet().contains(element)) {
            model.selection.changeWeight(element, -rotation);
        }
    }

    /**
     * Toggle selection state on mouse click.
     */
    @Override
    public void mouseClicked() {
        // Open website on ctrl click for relevant sets.
        if(mouseEvent().isControlDown()) {
            // URL to open.
            String url = element.url;
            
            if(element instanceof HSet) {
                // TODO: fix this. For identifiers and specified URL.
                
                /*
                GOTerm term = ((GOProteinSet) element).term;
                
                url = term.hyperLink; //"http://amigo.geneontology.org/cgi-bin/amigo/term_details?term=" + term.id;
                */
            }
            
            // Try to open browser if URL is specified.
            if(url != null && url.trim().length() > 0) {
                try {
                    Desktop.getDesktop().browse(URI.create(url.trim()));
                } catch(IOException ex) {
                    Logger.getLogger(SetRepresentation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        // Select otherwise.
        else {
            model.selection.select(element);
        }
    }
    
}

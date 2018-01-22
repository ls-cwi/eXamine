package org.cytoscape.examine.internal.visualization;

import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import org.cytoscape.examine.internal.model.Model;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

// ProteinSet representation.
public abstract class SetRepresentation extends Representation<HSet> {

    private final Model model;
    
    // Base constructor.
    public SetRepresentation(Model model, HSet element) {
        super(element);

        this.model = model;
    }
    
    public boolean highlight() {
        return model.highlightedSets.get().contains(element);
    }

    @Override
    // Highlight term and its member proteins.
    public void beginHovered() {
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
    // Adjust weight if set is selected.
    public void mouseWheel(int rotation) {
        if(model.selection.activeSetMap.keySet().contains(element)) {
            model.selection.changeWeight(element, -rotation);
        }
    }

    @Override
    // Toggle selection state on mouse click.
    public void mouseClicked(MouseEvent e) {
        // Open website on ctrl click for relevant sets.
        if(e.isControlDown()) {
            // URL to open.
            String url = element.url;
            
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

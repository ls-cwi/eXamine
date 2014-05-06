package org.cytoscape.examine.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import org.cytoscape.examine.internal.signal.Observer;
import org.cytoscape.examine.internal.signal.Variable;
import org.cytoscape.examine.internal.signal.VolatileSet;

import static org.cytoscape.examine.internal.Modules.*;
import java.util.List;
import java.util.Set;

import org.cytoscape.examine.internal.Constants;
import org.cytoscape.examine.internal.ViewerAction;
import org.cytoscape.examine.internal.data.HCategory;
import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.data.Network;
import org.cytoscape.examine.internal.data.SuperNetwork;
import org.cytoscape.examine.internal.signal.Volatile;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

/**
 * Model module. Manages enriched data that includes
 * information provided via user interaction.
 */
public final class Model {
    
    // Selected (visualized) sets.
    public final Selection selection;
    
    // Opened set categories.
    public final VolatileSet<HCategory> openedCategories;
    public final Variable<List<HCategory>> orderedCategories;
    
    // Highlighted proteins.
    public final VolatileSet<HNode> highlightedProteins;
    
    // Highlighted interactions.
    public final VolatileSet<DefaultEdge> highlightedInteractions;
    
    // Highlighted protein sets.
    public final VolatileSet<HSet> highlightedSets;
    
    // Active network.
    public final Variable<Network> activeNetwork;
    
    
    
    // Selection mode.
    private Constants.Selection selectionMode;
    
    public Constants.Selection getSelectionMode() {
		return selectionMode;
	}

	public void setSelectionMode(Constants.Selection selectionMode) {
		this.selectionMode = selectionMode;
	}

    /**
     * Base constructor.
     */
    public Model() {
        this.selection = new Selection(this);
        this.openedCategories = new VolatileSet<HCategory>();
        this.orderedCategories =
                new Variable<List<HCategory>>(Collections.<HCategory>emptyList());
        this.highlightedProteins = new VolatileSet<HNode>();
        this.highlightedInteractions = new VolatileSet<DefaultEdge>();
        this.highlightedSets = new VolatileSet<HSet>();
        this.activeNetwork = new Variable<Network>(new SuperNetwork(null,
                                new Pseudograph<HNode, DefaultEdge>(DefaultEdge.class)));
        this.selectionMode = Constants.Selection.NONE;
    }
    
    public void initListeners() {
        
        // Update active network that is to be visualized.
        Observer activeNetworkObserver = new Observer() {

            @Override
            public void signal() {              
                activeNetwork.set(data.superNetwork.get());
            }
            
        };
        
        //Parameters.visualStaticProteinBasis.change.subscribe(activeNetworkObserver);
        selection.change.subscribe(activeNetworkObserver);
        data.superNetwork.change.subscribe(activeNetworkObserver);
        data.categories.change.subscribe(activeNetworkObserver);
        
        // Update ordered category list.
        Observer categoryObserver = new Observer() {

            public void signal() {
                List<HCategory> openedCat = new ArrayList<HCategory>();
                List<HCategory> closedCat = new ArrayList<HCategory>();
                for(HCategory c: data.categories.get().values()) {
                    if(openedCategories.get().contains(c)) {
                        openedCat.add(c);
                    } else {
                        closedCat.add(c);
                    }
                }
                
                openedCat.addAll(closedCat);
                
                orderedCategories.set(openedCat);
            }
            
        };
        
        openedCategories.change.subscribe(categoryObserver);
        data.categories.change.subscribe(categoryObserver);
        
        // Update Cytoscape selection view.
        selection.change.subscribe(new Observer() {

            public void signal() {
                if (selectionMode != Constants.Selection.NONE) {
	                Set<HNode> selectedNode = 
	                		selection.selectedNodes(selectionMode == Constants.Selection.INTERSECTION);
	
	                // Deselect currently selected nodes.
	                CyNetwork cyNetwork = data.superNetwork.get().cyNetwork;
	                List<CyNode> cyNodes = CyTableUtil.getNodesInState(cyNetwork, CyNetwork.SELECTED, true);
	                for (CyNode node: cyNodes) {
	                    cyNetwork.getRow(node).set(CyNetwork.SELECTED, false);
	                }
	
	                // Select nodes.
	                for (HNode node: selectedNode) {
	                    cyNetwork.getRow(node.cyNode).set(CyNetwork.SELECTED, true);
	                }
	                
	                // Update Cytoscape view for new selection.
	                ViewerAction.applicationManager.getCurrentNetworkView().updateView();
                }
            }
        });
    }
}

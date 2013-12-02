package org.cytoscape.examine.internal.model;

import aether.signal.Observer;
import aether.signal.Variable;
import aether.signal.VolatileSet;
import java.util.Collection;

import static org.cytoscape.examine.internal.Modules.*;
import static org.cytoscape.examine.internal.visualization.Parameters.StaticProteinBasis.Intersection;
import static org.cytoscape.examine.internal.visualization.Parameters.StaticProteinBasis.Union;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.examine.internal.Constants;
import org.cytoscape.examine.internal.ViewerAction;
import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.data.Network;
import org.cytoscape.examine.internal.data.SuperNetwork;
import org.cytoscape.examine.internal.visualization.Parameters;
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
    
    // Highlighted proteins.
    public final VolatileSet<HNode> highlightedProteins;
    
    // Highlighted protein sets.
    public final VolatileSet<HSet> highlightedSets;
    
    // Active network.
    public final Variable<Network> activeNetwork;
    
    // Selection mode
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
        this.highlightedProteins = new VolatileSet<HNode>();
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

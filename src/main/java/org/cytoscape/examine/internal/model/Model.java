package org.cytoscape.examine.internal.model;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.examine.internal.Constants;
import org.cytoscape.examine.internal.data.DataSet;
import org.cytoscape.examine.internal.data.HCategory;
import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.data.Network;
import org.cytoscape.examine.internal.signal.Observer;
import org.cytoscape.examine.internal.signal.Variable;
import org.cytoscape.examine.internal.signal.VolatileSet;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Manages enriched data that includes information provided via user interaction.
 */
public final class Model {

    private final DataSet dataSet;
    private final CyApplicationManager applicationManager;
    private final VisualMappingManager visualMappingManager;
    private final CyGroupManager groupManager;

    public final Selection selection;                               // Selected (visualized) sets.
    public final VolatileSet<HCategory> openedCategories;           // Opened set categories.
    public final Variable<List<HCategory>> orderedCategories;
    public final VolatileSet<HNode> highlightedProteins;            // Highlighted proteins.
    public final VolatileSet<DefaultEdge> highlightedInteractions;  // Highlighted interactions.
    public final VolatileSet<HSet> highlightedSets;                 // Highlighted protein sets.
    public final Variable<Network> activeNetwork;                   // Active network.
    public final Variable<Boolean> showScore;                       // Whether to show the statistical score.
    private Constants.Selection selectionMode;                      // Selection mode.

    public Model(
            DataSet dataSet,
            CyApplicationManager applicationManager,
            VisualMappingManager visualMappingManager,
            CyGroupManager groupManager,
            boolean showScore,
            Constants.Selection selectionMode) {

        this.dataSet = dataSet;
        this.applicationManager = applicationManager;
        this.visualMappingManager = visualMappingManager;
        this.groupManager = groupManager;

        this.selection = new Selection(this);
        this.openedCategories = new VolatileSet<HCategory>();
        this.orderedCategories =
                new Variable<List<HCategory>>(Collections.<HCategory>emptyList());
        this.highlightedProteins = new VolatileSet<HNode>();
        this.highlightedInteractions = new VolatileSet<DefaultEdge>();
        this.highlightedSets = new VolatileSet<HSet>();
        this.activeNetwork = new Variable<Network>(dataSet.superNetwork.get());
        this.showScore = new Variable<Boolean>(showScore);
        this.selectionMode = selectionMode;

        // Transfer selected group nodes to HSet selection.
        final CyTable groupTable = dataSet.superNetwork.get().cyNetwork.getDefaultNodeTable();
        for (final HCategory category : dataSet.categories.get().values()) {
            for (final HSet set : category.members) {
                if (groupTable.getRow(set.cyGroup.getGroupNode().getSUID()).get(CyNetwork.SELECTED, Boolean.class)) {
                    System.out.println(set.name + " is selected");
                    selection.add(set, 1);
                } else {
                    System.out.println(set.name + " is not selected");
                }
            }
        }

        // Parameters are now set up => connect model listeners.
        initListeners();
    }

    private void initListeners() {

        // Update ordered category list.
        Observer categoryObserver = new Observer() {

            public void signal() {
                List<HCategory> openedCat = new ArrayList<HCategory>();
                List<HCategory> closedCat = new ArrayList<HCategory>();
                for (HCategory c : dataSet.categories.get().values()) {
                    if (openedCategories.get().contains(c)) {
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
        dataSet.categories.change.subscribe(categoryObserver);

        // Update Cytoscape selection view.
        selection.change.subscribe(new Observer() {
            public void signal() {
                final CyNetwork cyNetwork = dataSet.superNetwork.get().cyNetwork;

                if (selectionMode != Constants.Selection.NONE) {
                    Set<HNode> selectedNode =
                            selection.selectedNodes(selectionMode == Constants.Selection.INTERSECTION);

                    // Deselect currently selected nodes.
                    List<CyNode> cyNodes = CyTableUtil.getNodesInState(cyNetwork, CyNetwork.SELECTED, true);
                    for (CyNode node : cyNodes) {
                        cyNetwork.getRow(node).set(CyNetwork.SELECTED, false);
                    }

                    // Select nodes.
                    for (HNode node : selectedNode) {
                        cyNetwork.getRow(node.cyNode).set(CyNetwork.SELECTED, true);
                    }

                    // Update Cytoscape view for new selection.
                    applicationManager.getCurrentNetworkView().updateView();
                }

                // Active sets to selected CyGroups.
                final CyTable groupTable = cyNetwork.getDefaultNodeTable();
                for (final HCategory category : dataSet.categories.get().values()) {
                    for (final HSet set : category.members) {
                        groupTable.getRow(set.cyGroup.getGroupNode().getSUID()).set(CyNetwork.SELECTED, false);
                    }
                }
                for (final HSet activeSet : selection.activeSetList) {
                    groupTable.getRow(activeSet.cyGroup.getGroupNode().getSUID()).set(CyNetwork.SELECTED, true);
                    System.out.println("Select " + activeSet);
                }
            }
        });
    }

    public <V> V styleValue(VisualProperty<V> property, CyRow cyRow) {
        V result;

        VisualStyle style = visualMappingManager.getCurrentVisualStyle();
        VisualMappingFunction<?, V> colorFunction = style.getVisualMappingFunction(property);
        if (colorFunction != null) {
            result = colorFunction.getMappedValue(cyRow);
        } else {
            result = style.getDefaultValue(property);
        }

        return result;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

}

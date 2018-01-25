package org.cytoscape.examine.internal.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.cytoscape.examine.internal.Constants;
import org.cytoscape.examine.internal.ControlPanel;
import org.cytoscape.examine.internal.CyReferences;
import org.cytoscape.examine.internal.Utilities;
import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListMultipleSelection;

/**
 * This class contains all the logic to generate groups.
 * 
 * @author melkebir
 * 
 */
public class GenerateGroups implements ObservableTask, TunableValidator {
	
	//Arguments
	
	/**
	 * The CyNetwork on which the groups are to be created
	 */
	@Tunable(description="The network from which the groups are to be created", context="nogui")
	public CyNetwork network = null;

	/**
	 * The columns from which the groups are to be created
	 */
	@Tunable(context="nogui", description="The columns from which the groups are to be generated")
	public ListMultipleSelection<String> selectedGroupColumns = null;
	
	@Tunable(description="Use all nodes (not only selected nodes)",context="nogui")
	public boolean useAllNodes = false;
	
	
	//Internal
	
	private Map<String, CyGroup> groupIndex;
	
	
	//Links
	
	private CyReferences references = CyReferences.getInstance();
	
	/**
	 * Default constructor, can be used for argument passing via Tunables
	 */
	public GenerateGroups() {
		//Populate with default values
		this.network = references.getApplicationManager().getCurrentNetwork();
        this.selectedGroupColumns = Utilities.populateColumnList(network);
	};

	/**
	 * Alternative constructor, used if arguments are known at the time of construction (for instance when called via GUI)
	 * @param network
	 * @param selectedGroupColumnNames
	 * @param all
	 */
	public GenerateGroups(
			CyNetwork network,
			List<String> selectedGroupColumnNames,
			boolean all) {
        this.network = network;
        this.selectedGroupColumns = new ListMultipleSelection<String>(selectedGroupColumnNames);
        //TODO: I don't like this workaround much
        this.selectedGroupColumns.setSelectedValues(this.selectedGroupColumns.getPossibleValues());
        this.useAllNodes = all;
	}
	

	/**
     * Initialize group index.
     */
    private void initGroupIndex(CyNetwork network) {
        groupIndex = new HashMap<String, CyGroup>();
        Set<CyGroup> groups = references.getGroupManager().getGroupSet(network);
        
        for (CyGroup group : groups) {
        	long SUID = group.getGroupNode().getSUID();
        	CyRow row = network.getDefaultNodeTable().getRow(SUID);
        	
        	String groupName = row.get(CyNetwork.NAME, String.class);
        	groupIndex.put(groupName, group);
        }
    }
	
    /**
     * Add/replace group with given name and member nodes.
     */
    private CyGroup addGroup(CyNetwork network, String groupName, List<CyNode> members) {
        // Determine whether group already exists.
        CyGroup group = groupIndex.get(groupName);
        if (group == null) {
            group = references.getGroupFactory().createGroup(network, members, new ArrayList<CyEdge>(), false);
            network.getDefaultNodeTable().getRow(group.getGroupNode().getSUID()).set(CyNetwork.NAME, groupName);
            
            groupIndex.put(groupName, group);
        } else {
        	members.removeAll(group.getNodeList());
        	
        	//System.out.println("Adding " + members.size() + " nodes.");
        	if (members.size() > 0)
        		group.addNodes(members);
            //System.out.println("Done!");
        }
        
        return group;
    }

	@Override
	public void cancel() {
		//TODO: Any states that need to be saved/cleaned?
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		/*taskMonitor.setTitle("Testing...");
		for (int i = 0; i < 5; i++) {
			taskMonitor.setStatusMessage("Sleeping #" + i);
			System.out.println("Sleeping #" + i);
			Thread.sleep(5000);
			taskMonitor.setProgress((double) i / (double) 4);
		}
		return;*/
		
		if (network == null) {
			throw new Exception("Network is null, this should never be the case!");
		}

		// Listeners are temporarily disabled to prevent firing of events throughout group generation
		ControlPanel.listenersEnabled.set(false);
		
		taskMonitor.setTitle("Generating groups");
		
		//Initializing group index
		taskMonitor.setStatusMessage("Initializing group index.");
		initGroupIndex(network);
		
		Map<String, Map<String, List<CyNode>>> map = new HashMap<String, Map<String,List<CyNode>>>();
		for (String groupColumnName : selectedGroupColumns.getSelectedValues()) {
			map.put(groupColumnName, new HashMap<String, List<CyNode>>());
		}
		
		taskMonitor.setStatusMessage("Extracting groups from selected columns.");
		// iterate over all rows, skipping group nodes
		List<CyRow> rows = network.getDefaultNodeTable().getAllRows();
		for (CyRow row : rows) {
			CyNode node = network.getNode(row.get(CyNetwork.SUID, Long.class));
			
			// skip group nodes
			if (node == null || references.getGroupManager().isGroup(node, network)) continue;
			// skip nodes that are not selected if !all
			if (!useAllNodes && !row.get(CyNetwork.SELECTED, Boolean.class)) continue;
			
			// iterate over all columns representing different categories
			for (String groupColumnName : selectedGroupColumns.getSelectedValues()) {
				
				Map<String, List<CyNode>> mapGroup = map.get(groupColumnName);
				//System.out.println(groupColumnName + ": " + node.getSUID());
				
				// iterate over all values in the current list column
				List<String> values = row.getList(groupColumnName, String.class, new ArrayList<String>());
				if (values != null) {
					for (String value : values) {
						List<CyNode> list = mapGroup.get(value);
						if (list == null) {
							list = new ArrayList<CyNode>();
							mapGroup.put(value, list);
						}
						
						// add node to the group
						list.add(node);
					}
				}
			}
		}
		
		// now we create the actual groups
		int i = 0;
		for (String groupColumnName : selectedGroupColumns.getSelectedValues()) {
			Map<String, List<CyNode>> mapGroup = map.get(groupColumnName);
			
			List<CyNode> subGroupNodes = new ArrayList<CyNode>();
			Set<String> keys = mapGroup.keySet();
			for (String groupName : keys) {
                List<CyNode> group = mapGroup.get(groupName);
                
    			taskMonitor.setStatusMessage("Adding group " + groupName + " " + " with " + group.size() + " entries.");
    			//System.out.println("Adding group " + groupColumnName + ": " + groupName + " with " + group.size() + " entries.");
                CyGroup subGroup = addGroup(network, groupName, group);
                subGroupNodes.add(subGroup.getGroupNode());
            }
			
			// Add column group.
			taskMonitor.setStatusMessage("Adding column group " + groupColumnName + " with " + subGroupNodes.size() + " entries.");
			//System.out.println("Adding column group " + groupColumnName + " with " + subGroupNodes.size() + " entries.");
			addGroup(network, Constants.CATEGORY_PREFIX + groupColumnName, subGroupNodes);
			
			i++;
			taskMonitor.setProgress((double) i / (double) selectedGroupColumns.getSelectedValues().size());
		}
		
		// now finalize groups
		taskMonitor.setStatusMessage("Finalizing groups.");
		references.getGroupManager().addGroups(new ArrayList<CyGroup>(groupIndex.values()));
		
		ControlPanel.listenersEnabled.set(true);
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		// TODO Auto-generated method stub; Should we return something here? Maybe at least a Boolean as a success indicator
		return null;
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		try {
			//Checks if the arguments are valid and acceptable
			if (network == null) {
				errMsg.append("Attempted to generate groups on an empty network!");
				return ValidationState.INVALID;
			}
			//Check the column names for validity
			if (selectedGroupColumns == null) {
				errMsg.append("You need to provide at least one column name to generate groups!");
				return ValidationState.INVALID;
			}
			if (selectedGroupColumns.getSelectedValues().size() <= 0) {
				errMsg.append("You need to provide at least one column name to generate groups!");
				return ValidationState.INVALID;
			}
			//TODO: Only allow group columns, link to NetworkSettings somehow?
			CyTable nodeTable = network.getDefaultNodeTable();
			for (String columnName: selectedGroupColumns.getSelectedValues()) {
				if (nodeTable.getColumn(columnName) == null) {
					errMsg.append("The column with name: "+columnName+" does not exist!");
					return ValidationState.INVALID;
				}
			}
			//If nothing bad happened, the arguments are acceptable and can be processed
			return ValidationState.OK;
		}
		catch (IOException ex) {
			ex.printStackTrace(); //TODO: Or is there a logger/ dedicated output stream for those in eXamine?
		}
		return ValidationState.INVALID;
	}
}

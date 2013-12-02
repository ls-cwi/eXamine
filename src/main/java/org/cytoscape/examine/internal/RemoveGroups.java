package org.cytoscape.examine.internal;

import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

/**
 * This class contains all the logic to remove groups.
 * 
 * @author melkebir
 * 
 */
public class RemoveGroups implements Task {
	private final CyGroupManager groupManager;
	private final CyNetwork network;
	
	public RemoveGroups(CyGroupManager groupManager, CyNetwork network) {
		this.groupManager = groupManager;
		this.network = network;
	}
	
	@Override
	public void cancel() {
		// TODO Auto-generated method stub
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (network == null) return;
		
		taskMonitor.setStatusMessage("Removing groups.");
		int i = 0;
		Set<CyGroup> groupSet = groupManager.getGroupSet(network);
		for (CyGroup group : groupSet) {
			groupManager.destroyGroup(group);
			
			i++;
			taskMonitor.setProgress((double) i / (double) groupSet.size());
		}
	}
}

package org.cytoscape.examine.internal.tasks;

import java.io.IOException;
import java.util.Set;

import org.cytoscape.examine.internal.CyReferences;
import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

/**
 * This class contains all the logic to remove groups.
 * 
 * @author melkebir
 * 
 */
public class RemoveGroups implements ObservableTask,TunableValidator {
	
	@Tunable(description="The network for which all groups are to be removed",context="nogui",required=true)
	public CyNetwork network;
	
	private final CyReferences references = CyReferences.getInstance();
	
	public RemoveGroups(CyNetwork network) {
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
		Set<CyGroup> groupSet = references.getGroupManager().getGroupSet(network);
		for (CyGroup group : groupSet) {
			references.getGroupManager().destroyGroup(group);
			i++;
			taskMonitor.setProgress((double) i / (double) groupSet.size());
		}
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		try {
			//Checks if the arguments are valid and acceptable
			if (network == null) {
				errMsg.append("Attempted to remove groups on an empty network!");
				return ValidationState.INVALID;
			}
			//If nothing bad happened, the arguments are acceptable and can be processed
			return ValidationState.OK;
		}
		catch (IOException ex) {
			ex.printStackTrace(); //TODO: Or is there a logger/ dedicated output stream for those in eXamine?
		}
		return ValidationState.INVALID;
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		// TODO Auto-generated method stub
		return null;
	}
}

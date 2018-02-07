package org.cytoscape.examine.internal.tasks;

import org.cytoscape.examine.internal.CyServices;
import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListMultipleSelection;

import java.util.Set;
import java.util.stream.Collectors;

public class SelectGroups implements ObservableTask, TunableValidator {

    @Tunable(description="The identifiers of the groups that are selected in the visualization; provide as comma-separated list, for instance selectedGroups=\"a,b,c\"; invalid list entries (that are not fitting group identifiers) are ignored", context="nogui")
    public ListMultipleSelection<String> selectedGroups = null;

    private final CyServices services;

    public SelectGroups(CyServices services) {
        this.services = services;
    }

    @Override
    public <R> R getResults(Class<? extends R> aClass) {
        return null;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {

        if (selectedGroups != null) {
            CyNetwork network = services.getApplicationManager().getCurrentNetwork();
            CyColumn keyColumn = network.getDefaultNodeTable().getPrimaryKey();
            Set<CyGroup> groups = services.getGroupManager().getGroupSet(network);
            Set<String> selectedGroupSet = selectedGroups.getSelectedValues().stream().collect(Collectors.toSet());

            for (CyGroup group : groups) {
                CyRow groupRow = network.getDefaultNodeTable().getRow(group.getGroupNode().getSUID());
                groupRow.set(CyNetwork.SELECTED, selectedGroupSet.contains(groupRow.get(keyColumn.getName(), String.class)));
            }
        }
    }

    @Override
    public void cancel() {

    }

    @Override
    public ValidationState getValidationState(Appendable appendable) {
        return ValidationState.OK;
    }
}

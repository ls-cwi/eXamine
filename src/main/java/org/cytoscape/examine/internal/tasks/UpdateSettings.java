package org.cytoscape.examine.internal.tasks;

import org.cytoscape.examine.internal.CyServices;
import org.cytoscape.examine.internal.Utilities;
import org.cytoscape.examine.internal.settings.NetworkSettings;
import org.cytoscape.examine.internal.settings.SessionSettings;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListMultipleSelection;

import java.util.List;
import java.util.stream.Collectors;

public class UpdateSettings implements ObservableTask, TunableValidator {

    @Tunable(description="The network from which the groups are to be created", context="nogui")
    public CyNetwork network;

    @Tunable(description = "The column that contains the labels that are displayed on nodes", context = "nogui")
    public String labelColumn = null;

    @Tunable(description = "The column that contains URLs for more information about nodes", context = "nogui")
    public String urlColumn = null;

    @Tunable(description = "The column that contains annotation enrichment scores", context = "nogui")
    public String scoreColumn = null;

    @Tunable(description = "Show enrichment score of annotations", context = "nogui")
    public Boolean showScore = null;

    @Tunable(description="The group columns that are shown in the visualization; provide as comma-separated list, for instance selectedGroupColumns=\"a,b,c\"; invalid list entries (that are not fitting column names) are ignored", context="nogui")
    public ListMultipleSelection<String> selectedGroupColumns;

    //private final CyServices services;
    private final SessionSettings settings;

    public UpdateSettings(CyServices services, SessionSettings settings) {
        //this.services = services;
        this.settings = settings;
        this.network = services.getApplicationManager().getCurrentNetwork();
        this.selectedGroupColumns = Utilities.populateColumnList(network);
    }

    @Override
    public <R> R getResults(Class<? extends R> aClass) {
        return null;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {

        NetworkSettings networkSettings = settings.getNetworkSettings(network);

        CyColumn labelCyColumn = columnByName(labelColumn);
        if (labelCyColumn != null) {
            networkSettings.setSelectedLabelColumn(labelCyColumn);
        }

        CyColumn urlCyColumn = columnByName(urlColumn);
        if (urlCyColumn != null) {
            networkSettings.setSelectedURLColumn(urlCyColumn);
        }

        CyColumn scoreCyColumn = columnByName(scoreColumn);
        if (scoreCyColumn != null) {
            networkSettings.setSelectedScoreColumn(scoreCyColumn);
        }

        if (showScore != null) {
            networkSettings.setShowScore(showScore);
        }

        if (selectedGroupColumns != null) {
            List<CyColumn> selectGroupCyColumns = selectedGroupColumns.getSelectedValues().stream()
                    .map(this::columnByName)
                    .filter(group -> group != null)
                    .collect(Collectors.toList());
            networkSettings.setSelectedGroupColumns(selectGroupCyColumns);
        }
    }

    private CyColumn columnByName(String columnName) {
        return columnName == null ?
                null :
                network.getDefaultNodeTable().getColumn(columnName);
    }

    @Override
    public void cancel() {

    }

    @Override
    public ValidationState getValidationState(Appendable appendable) {
        return ValidationState.OK;
    }
}

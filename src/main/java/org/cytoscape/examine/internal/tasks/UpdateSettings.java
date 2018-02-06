package org.cytoscape.examine.internal.tasks;

import org.cytoscape.examine.internal.CyServices;
import org.cytoscape.examine.internal.settings.NetworkSettings;
import org.cytoscape.examine.internal.settings.SessionSettings;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class UpdateSettings implements ObservableTask, TunableValidator {

    @Tunable(description = "The column that contains the labels that are displayed on nodes", context = "nogui")
    public String labelColumn = null;

    @Tunable(description = "The column that contains URLs for more information about nodes", context = "nogui")
    public String urlColumn = null;

    @Tunable(description = "The column that contains annotation enrichment scores", context = "nogui")
    public String scoreColumn = null;

    @Tunable(description = "Show enrichment score of annotations", context = "nogui")
    public Boolean showScore = null;

    private final CyServices services;
    private final SessionSettings settings;

    public UpdateSettings(CyServices services, SessionSettings settings) {
        this.services = services;
        this.settings = settings;
    }

    @Override
    public <R> R getResults(Class<? extends R> aClass) {
        return null;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {

        NetworkSettings networkSettings = settings.getNetworkSettings(
                services.getApplicationManager().getCurrentNetwork());
//
//        if (labelColumn != null) {
//            networkSettings.setSelectedLabelColumnName(labelColumn);
//        }
//        if (urlColumn != null) {
//            networkSettings.setSelectedURLColumnName(urlColumn);
//        }
//        if (scoreColumn != null) {
//            networkSettings.setSelectedScoreColumnName(scoreColumn);
//        }
//        if (showScore != null) {
//            networkSettings.setShowScore(showScore);
//        }
    }

    @Override
    public void cancel() {

    }

    @Override
    public ValidationState getValidationState(Appendable appendable) {
        return ValidationState.OK;
    }
}

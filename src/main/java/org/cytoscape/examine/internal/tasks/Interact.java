package org.cytoscape.examine.internal.tasks;

import org.cytoscape.examine.internal.CyServices;
import org.cytoscape.examine.internal.Utilities;
import org.cytoscape.examine.internal.settings.SessionSettings;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableValidator;

public class Interact implements ObservableTask, TunableValidator {

    private final CyServices services;
    private final SessionSettings settings;

    public Interact(CyServices services, SessionSettings settings) {
        this.services = services;
        this.settings = settings;
    }

    @Override
    public <R> R getResults(Class<? extends R> aClass) {
        return null;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        Utilities.openVisualizationWindow(
                services,
                settings.getNetworkSettings(services.getApplicationManager().getCurrentNetwork()));
    }

    @Override
    public void cancel() {

    }

    @Override
    public ValidationState getValidationState(Appendable appendable) {
        return ValidationState.OK;
    }
}

package org.cytoscape.examine.internal.tasks;

import org.cytoscape.examine.internal.CyServices;
import org.cytoscape.examine.internal.Utilities;
import org.cytoscape.examine.internal.settings.SessionSettings;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

import java.io.File;

/** Exports an image of the eXamine visualization. */
public class ExportImage implements ObservableTask, TunableValidator {

    @Tunable(description="The file path to export to", context="nogui")
    public String path;

    private final CyServices services;
    private final SessionSettings settings;

    public ExportImage(CyServices services, SessionSettings settings) {
        this.services = services;
        this.settings = settings;
    }

    @Override
    public <R> R getResults(Class<? extends R> aClass) {
        return null;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        Utilities.exportVisualization(
                services,
                settings.getNetworkSettings(services.getApplicationManager().getCurrentNetwork()),
                new File(path));
    }

    @Override
    public void cancel() {

    }

    @Override
    public ValidationState getValidationState(Appendable appendable) {
        return ValidationState.OK;
    }
}

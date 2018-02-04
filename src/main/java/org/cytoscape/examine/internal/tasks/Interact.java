package org.cytoscape.examine.internal.tasks;

import org.cytoscape.examine.internal.CyServices;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableValidator;

public class Interact implements ObservableTask, TunableValidator {

    private final CyServices services;

    public Interact(CyServices services) {
        this.services = services;
    }

    @Override
    public <R> R getResults(Class<? extends R> aClass) {
        return null;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {

    }

    @Override
    public void cancel() {

    }

    @Override
    public ValidationState getValidationState(Appendable appendable) {
        return ValidationState.OK;
    }
}

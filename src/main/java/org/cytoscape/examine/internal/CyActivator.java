package org.cytoscape.examine.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.examine.internal.settings.SessionSettings;
import org.cytoscape.examine.internal.taskfactories.CommandTaskFactory;
import org.cytoscape.examine.internal.tasks.ExamineCommand;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;

/**
 * Execution body.
 */
public class CyActivator extends AbstractCyActivator {

    // TODO: is there a way to persist these settings across Cytoscape runs?
    private final SessionSettings settings = new SessionSettings();

    /**
     * Upon bundle activation (install or startup).
     */
    public void start(BundleContext bundleContext) {

        // Show eXamine control panel
        CyServices services = createServices(bundleContext);
        ControlPanel controlPanel = new ControlPanel(services, settings);

        // Register it as a service.
        registerService(bundleContext, controlPanel, CytoPanelComponent.class, new Properties());
        registerService(bundleContext, controlPanel, SetCurrentNetworkListener.class, new Properties());
        registerService(bundleContext, controlPanel, RowsSetListener.class, new Properties());
        registerService(bundleContext, controlPanel, ColumnNameChangedListener.class, new Properties());
        registerService(bundleContext, controlPanel, ColumnDeletedListener.class, new Properties());
        registerService(bundleContext, controlPanel, ColumnCreatedListener.class, new Properties());
        registerService(bundleContext, controlPanel, NetworkDestroyedListener.class, new Properties());
        registerService(bundleContext, controlPanel, SessionLoadedListener.class, new Properties());

        //Register commands to allow access via CyRest TODO: Possible to reduce number of lines by putting shared lines in a function, this might be easier to read though
        registerCommands(bundleContext,
                ExamineCommand.GENERATE_GROUPS,
                ExamineCommand.REMOVE_GROUPS,
                ExamineCommand.UPDATE_SETTINGS,
                ExamineCommand.SELECT_GROUPS,
                ExamineCommand.INTERACT,
                ExamineCommand.EXPORT
        );
    }

    /**
     * Extracts Cytoscape services from a bundle context for propagation.
     * TODO: Remove redundant fields throughout app
     */
    private CyServices createServices(BundleContext bundleContext) {
        return new CyServices(
                getService(bundleContext, CyNetworkManager.class),
                getService(bundleContext, CyRootNetworkManager.class),
                getService(bundleContext, CyApplicationManager.class),
                getService(bundleContext, CySessionManager.class),
                getService(bundleContext, CyGroupManager.class),
                getService(bundleContext, CyGroupFactory.class),
                getService(bundleContext, DialogTaskManager.class),
                getService(bundleContext, VisualMappingManager.class)
        );
    }

    /**
     * Registers the commands with the bundle context and makes them accesible via CyRest
     *
     * @param bc
     * @param commands The commands that are to be registered
     */
    private void registerCommands(BundleContext bc, ExamineCommand... commands) {
        CyServices services = createServices(bc);

        for (ExamineCommand command : commands) {
            TaskFactory commandTaskFactory = new CommandTaskFactory(services, settings, command);
            Properties props = new Properties();
            props.setProperty(COMMAND_NAMESPACE, Constants.APP_COMMAND_PREFIX);
            props.setProperty(COMMAND, command.toString());
            props.setProperty(COMMAND_DESCRIPTION, command.getDescription());
            registerService(bc, commandTaskFactory, TaskFactory.class, props);
        }
    }

}

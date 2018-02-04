package org.cytoscape.examine.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.swing.DialogTaskManager;

import static java.util.Objects.requireNonNull;

/**
 * Contains references to Cytoscape services that need to be accessed throughout various parts of the application
 * This can be used to avoid passing those services as arguments throughout the application to keep method signatures shorter and comprehensible
 */
public class CyServices {

    private final CyNetworkManager networkManager;
    private final CyRootNetworkManager rootNetworkManager;
    private final CyApplicationManager applicationManager;
    private final CySessionManager sessionManager;
    private final CyGroupFactory groupFactory;
    private final CyGroupManager groupManager;
    private final DialogTaskManager taskManager;
    private final VisualMappingManager visualMappingManager;

    public CyServices(
            CyNetworkManager networkManager,
            CyRootNetworkManager rootNetworkManager,
            CyApplicationManager applicationManager,
            CySessionManager sessionManager,
            CyGroupManager groupManager,
            CyGroupFactory groupFactory,
            DialogTaskManager taskManager,
            VisualMappingManager visualMappingManager
    ) {
        this.networkManager = requireNonNull(networkManager);
        this.rootNetworkManager = requireNonNull(rootNetworkManager);
        this.applicationManager = requireNonNull(applicationManager);
        this.sessionManager = requireNonNull(sessionManager);
        this.groupManager = requireNonNull(groupManager);
        this.groupFactory = requireNonNull(groupFactory);
        this.taskManager = requireNonNull(taskManager);
        this.visualMappingManager = requireNonNull(visualMappingManager);
    }

    /**
     * Access to all networks.
     */
    public CyNetworkManager getNetworkManager() {
        return networkManager;
    }

    /**
     * Access to all root networks.
     */
    public CyRootNetworkManager getRootNetworkManager() {
        return rootNetworkManager;
    }

    /**
     * Basic access to current and/or currently selected networks,
     * views and rendering engines in an instance of Cytoscape.
     */
    public CyApplicationManager getApplicationManager() {
        return applicationManager;
    }

    /**
     * Access to session information.
     */
    public CySessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * The CyGroupManager maintains information about all of the groups an instance of Cytoscape.
     */
    public CyGroupManager getGroupManager() {
        return groupManager;
    }

    /**
     * An interface describing a factory used for creating CyGroup objects.
     */
    public CyGroupFactory getGroupFactory() {
        return groupFactory;
    }

    /**
     * Access to task functionality.
     */
    public DialogTaskManager getTaskManager() {
        return taskManager;
    }

    /**
     * This object manages mapping from view model to VisualStyle.
     * User objects can access all VisualStyles and VisualMappingFunctions through this class.
     */
    public VisualMappingManager getVisualMappingManager() {
        return visualMappingManager;
    }


}

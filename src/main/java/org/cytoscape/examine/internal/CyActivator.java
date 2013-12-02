package org.cytoscape.examine.internal;

import org.cytoscape.examine.internal.ViewerAction;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.osgi.framework.BundleContext;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.events.SessionLoadedListener;

import java.util.Properties;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskManager;

/**
 * Execution body.
 */
public class CyActivator extends AbstractCyActivator {

    /**
     * Base constructor.
     */
    public CyActivator() {
        super();
    }

    /**
     * Upon bundle activation (install or startup).
     */
    public void start(BundleContext bc) {
        // Manager services.
        //CySwingApplication desktopManager = getService(bc, CySwingApplication.class);
        
        // Basic access to current and/or currently selected networks, 
        // views and rendering engines in an instance of Cytoscape.
        CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
        
        // Access to all root networks
        CyRootNetworkManager rootNetworkManager = getService(bc, CyRootNetworkManager.class);
        
        // Access to all networks
        CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
        
        // This object manages mapping from view model to VisualStyle. 
        // User objects can access all VisualStyles and VisualMappingFunctions through this class.
        VisualMappingManager visualMappingManager = getService(bc, VisualMappingManager.class);
        
        // The CyGroupManager maintains information about all of the groups an instance of Cytoscape.
        CyGroupManager groupManager = getService(bc, CyGroupManager.class);
        
        // An interface describing a factory used for creating CyGroup objects.
        CyGroupFactory groupFactory = getService(bc, CyGroupFactory.class);
        
        TaskManager taskManager = getService(bc, TaskManager.class);
        
        // Action, the group viewer
        ViewerAction viewerAction =
                new ViewerAction(applicationManager,
                                 visualMappingManager,
                                 groupManager,
                                 groupFactory);
        
        // Action, the group selector
        /*GroupsFromColumnsAction groupsAction =
                new GroupsFromColumnsAction(applicationManager,
                                            groupManager,
                                            groupFactory);*/
        
        // The eXamine control panel
        ControlPanel controlPanel = new ControlPanel(networkManager, rootNetworkManager, 
        		applicationManager, groupManager, groupFactory, taskManager);

        // Register it as a service.
        registerService(bc, viewerAction, CyAction.class, new Properties());
        //registerService(bc, groupsAction, CyAction.class, new Properties());
        registerService(bc, controlPanel, CytoPanelComponent.class, new Properties());
        registerService(bc, controlPanel, SetCurrentNetworkListener.class, new Properties());
        registerService(bc, controlPanel, RowsSetListener.class, new Properties());
        registerService(bc, controlPanel, ColumnNameChangedListener.class, new Properties());
        registerService(bc, controlPanel, ColumnDeletedListener.class, new Properties());
        registerService(bc, controlPanel, ColumnCreatedListener.class, new Properties());
        registerService(bc, controlPanel, NetworkDestroyedListener.class, new Properties());
        registerService(bc, controlPanel, SessionLoadedListener.class, new Properties());
    }
    
    /**
     * Cleanup module resources. (Does this work?)
     */
    @Override
    protected void finalize() throws Throwable {
        Modules.dispose();
        super.finalize();
    }
    
}

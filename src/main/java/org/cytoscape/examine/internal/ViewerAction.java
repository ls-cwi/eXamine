package org.cytoscape.examine.internal;

import java.awt.event.ActionEvent;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
    
/**
 * Show the group viewer.
 */
public class ViewerAction extends AbstractCyAction {
	// Static references to managers, not pretty but effective, for now...
    public static CyApplicationManager applicationManager;
    public static VisualMappingManager visualMappingManager;
    public static CyGroupManager groupManager;
    public static CyGroupFactory groupFactory;

    public ViewerAction(CyApplicationManager applicationManager,
                        VisualMappingManager visualMappingManager,
                        CyGroupManager groupManager,
                        CyGroupFactory groupFactory) {
        super("View");

        // Set the preferred menu.
        //setPreferredMenu(Constants.APP_MENU_PATH);
        
        ViewerAction.applicationManager = applicationManager;
        ViewerAction.visualMappingManager = visualMappingManager;
        ViewerAction.groupManager = groupManager;
        ViewerAction.groupFactory = groupFactory;
    }

    public void actionPerformed(ActionEvent ae) {
        // Launch viewer (and sub-modules).
        Modules.initialize();
        updateData();
    }
    
    // Update application data.
    private void updateData() {
        //Modules.data.load(applicationManager.getCurrentNetwork(), groupManager);
        
        System.out.println("Loaded data:");
        System.out.println(Modules.data.categories.get().size() + " categories");
    }

}
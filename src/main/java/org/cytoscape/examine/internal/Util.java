package org.cytoscape.examine.internal;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

/**
 *
 */
public class Util {
    
    public static boolean checkNetwork(CyApplicationManager applicationManager) {
        boolean result = false;
        
        CyNetworkView view = applicationManager.getCurrentNetworkView();
        CyNetwork network = applicationManager.getCurrentNetwork();
            
        // Cannot continue if either of these is null.
        if (network == null || view == null) {
            JOptionPane.showMessageDialog(
                    null,
                    "No network and/or view to link to.",
                    "Missing network",
                    JDialog.ERROR);
        } else {
            result = true;
        }
        
        return result;
    }
    
}

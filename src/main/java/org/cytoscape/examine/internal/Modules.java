package org.cytoscape.examine.internal;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import static org.cytoscape.examine.internal.Modules.data;
import static org.cytoscape.examine.internal.Modules.dispose;
import static org.cytoscape.examine.internal.Modules.model;
import static org.cytoscape.examine.internal.Modules.visualization;

import org.cytoscape.examine.internal.data.DataSet;
import org.cytoscape.examine.internal.model.Model;
import org.cytoscape.examine.internal.visualization.Visualization;

/**
 * Primary module singletons.
 */
public class Modules {
    
    // Modules.
    public static DataSet data;
    public static Model model;
    public static Visualization visualization;
    
    // Ugly
    public static boolean showScore;
    
    /**
     * Initialize modules.
     */
    public static void initialize() {
        // Hack context loader.
        Thread.currentThread().setContextClassLoader(Modules.class.getClassLoader());
        
        // Initialize once.
        if(data == null) {
            data = new DataSet();
            model = new Model();
            initVisualization();
        }
    }
    
    private static void initVisualization() {
        visualization = new Visualization();
        
        // Override default Exit Application listener -> enforce hide.
        for(WindowListener wl: visualization.getWindowListeners()) {
            visualization.removeWindowListener(wl);
        }
        visualization.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
            
        });
        
        visualization.setVisible(true);
    }
    
    /**
     * Deinitialize modules.
     */
    public static void dispose() {
        if(visualization != null) {
            visualization.setVisible(false);
            visualization.dispose();
            try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        // todo: animation thread needs to be stopped first, i don't know how to do this
        visualization = null;
        model = null;
        data = null;
    }
    
}

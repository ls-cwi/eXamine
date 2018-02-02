package org.cytoscape.examine.internal;

/**
 * Important constants, such as metric names.
 */
public class Constants {
    
    // App symbolic name and menu path.
    public static final String APP_NAME = "eXamine";
    
    /**
     * The prefix used for all commands associated with this Cytoscape app
     */
    public static final String APP_COMMAND_PREFIX = "examine";
    
    // Cytoscape data form.
    public static final String CATEGORY_PREFIX = "Category_";
    
    // Category list size limit.
    public static final int CATEGORY_MAX_SIZE = 50;
    
    // Selection mechanism
    public enum Selection { 
    	NONE, 
    	UNION, 
    	INTERSECTION
    }
}

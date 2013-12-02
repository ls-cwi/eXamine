package org.cytoscape.examine.internal.data;

/**
 * Screen data element.
 */
abstract public class HElement {
    
    // User friendly name.
    public final String name;
    
    // Optional URL.
    public final String url;
    
    /**
     * Base constructor.
     */
    public HElement(String name, String url) {
        this.name = name;
        this.url = url;
    }
    
}

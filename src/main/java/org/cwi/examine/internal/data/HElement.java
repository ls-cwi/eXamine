package org.cwi.examine.internal.data;

/**
 * Screen data element.
 */
abstract public class HElement {

    public final String identifier;
    public final String name;   // User friendly name.
    public final String url;    // Optional URL.
    
    /**
     * Base constructor.
     */
    public HElement(final String identifier, final String name, final String url) {
        this.identifier = identifier;
        this.name = name;
        this.url = url;
    }
    
}

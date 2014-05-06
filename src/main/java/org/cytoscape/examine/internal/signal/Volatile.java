package org.cytoscape.examine.internal.signal;

/**
 * Contains a volatile value of type E of which a change
 * is propagated by a signal.
 */
public abstract class Volatile<E> {
    
    // Change signal.
    public final Subject change;
    
    /**
     * Base constructor.
     */
    public Volatile() {
        change = new Subject();
    }
    
    /*
     * Get value.
     */
    public abstract E get();
    
}

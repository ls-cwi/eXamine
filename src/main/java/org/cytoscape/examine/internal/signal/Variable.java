package org.cytoscape.examine.internal.signal;

/**
 * Contains a volatile value of type E that can be updated.
 */
public class Variable<E> extends Volatile<E> {
        
    // Wrapped value.
    private E value;
    
    /**
     * Base constructor.
     */
    public Variable(E initialValue) {
        value = initialValue;
    }
    
    /*
     * Get value.
     */
    @Override
    public E get() {
        return value;
    }
    
    /**
     * Set value. Only propagates a signal when the
     * new value is different from the old.
     */
    public void set(E newValue) {
        if(!(newValue == null && value == null) && (
            (newValue == null && value != null) ||
            !newValue.equals(value))) {
            value = newValue;
            change.signal();
        }
    }
    
}

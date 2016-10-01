package org.cwi.examine.internal.signal;

/**
 * Constant (wrapped) value that acts as a volatile.
 */
public class Constant<E> extends Volatile<E> {
    
    // Constant value.
    private E value;
    
    /**
     * Base constructor.
     */
    public Constant(E initialValue) {
        value = initialValue;
    }

    @Override
    public E get() {
        return value;
    }
    
}

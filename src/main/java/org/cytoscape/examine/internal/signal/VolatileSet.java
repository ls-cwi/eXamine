package org.cytoscape.examine.internal.signal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains a volatile set with elements of type E
 * of which a change is propagated by a signal.
 */
public class VolatileSet<E> extends Volatile<Set<E>> {
    
    // Wrapped set.
    private Set<E> set;
    
    /**
     * Base constructor. Initializes set with given elements.
     */
    public VolatileSet(E... elements) {
        set = new HashSet<E>();
        set.addAll(Arrays.asList(elements));
    }

    /**
     * Protect set from modifications.
     */
    @Override
    public Set<E> get() {
        return Collections.unmodifiableSet(set);
    }
    
    // --- Begin set delegated methods. ---

    public boolean add(E element) {
        boolean result = set.add(element);
        
        change.signal();
        
        return result;
    }

    public boolean remove(E element) {
        boolean result = set.remove(element);
      
        change.signal();
        
        return result;
    }

    public boolean addAll(Collection<E> elements) {
        boolean result = set.addAll(elements);
        
        change.signal();
        
        return result;
    }

    public boolean retainAll(Collection<E> elements) {
        boolean result = set.retainAll(elements);
        
        change.signal();
        
        return result;
    }

    public boolean removeAll(Collection<E> elements) {
        boolean result = set.removeAll(elements);
        
        change.signal();
        
        return result;
    }

    public void clear() {
        set.clear();
        
        change.signal();
    }
    
    public void set(Collection<E> elements) {
        set.clear();
        set.addAll(elements);
        
        change.signal();
    }
    
    // --- End set delegated methods. ---
    
}

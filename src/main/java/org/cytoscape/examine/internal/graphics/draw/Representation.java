package org.cytoscape.examine.internal.graphics.draw;

/**
 * Snippet that represents an element.
 */
public abstract class Representation<E> extends PositionedSnippet {

    // Represented element.
    public final E element;
    
    /**
     * Snippet wraps an element and has a topLeft corner.
     */
    public Representation(E element) {
        this.element = element;
    }
    
    /**
     * Identify snippet by element.
     */
    @Override
    public int hashCode() {
        return element.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof Representation<?>) {
    		Representation<?> otherRepr = (Representation<?>)obj;
    		if (otherRepr.getClass() == this.getClass()) {
    			if (this.element.equals(((Representation<?>) obj).element)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
}

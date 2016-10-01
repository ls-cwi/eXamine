package org.cwi.examine.internal.graphics.draw;

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
        return obj instanceof Representation &&
               this.getClass().equals(obj.getClass()) &&
               this.element.equals(((Representation) obj).element);
    }
    
}

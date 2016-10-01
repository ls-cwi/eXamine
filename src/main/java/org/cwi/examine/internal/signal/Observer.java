package org.cwi.examine.internal.signal;

/**
 * Observer that listens for a notification.
 */
public interface Observer {
    
    /**
     * Signal that is called by a Subject instance
     * that the observer is subscribed to.
     */
    public void signal();
    
}

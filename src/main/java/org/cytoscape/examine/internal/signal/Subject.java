package org.cytoscape.examine.internal.signal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Subject that subscribes Observers to which it
 * propagates signals.
 */
public class Subject {
    
    // Observers to propagate signal to.
    private Collection<Observer> observers;
    
    /**
     * Base constructor.
     */
    public Subject() {
        this.observers = new ArrayList<Observer>();
    }
    
    /**
     * Subscribe the given Observer.
     */
    public void subscribe(Observer observer) {
        observers.add(observer);
    }
    
    /**
     * Unsubscribe the given Observer.
     */
    public void unsubscribe(Observer observer) {
        observers.remove(observer);
    }
    
    /**
     * Propagate signal to observers.
     */
    public void signal() {
        for(Observer o: observers) {
            o.signal();
        }
    }
    
    
    /**
     * Join that combines and propagates signals
     * from multiple sources.
     */
    public static class SubjectJoin extends Subject {
    
        // Subject sources.
        private final Set<Subject> subjects;
        
        // Signal propagator,
        private Observer propagator;
        
        /**
         * Base constructor.
         */
        public SubjectJoin(Subject... sourceSubjects) {
            this.subjects = new HashSet<Subject>();
            this.subjects.addAll(Arrays.asList(sourceSubjects));
            this.propagator = new Observer() {

                    @Override
                    public void signal() {
                        SubjectJoin.this.signal();
                    }
                    
                };
            
            // Subscribe to sources and propagate signal.
            for(Subject s: subjects) {
                s.subscribe(propagator);
            }
        }
        
        /**
         * Add subject source.
         */
        public void add(Subject subject) {
            subjects.add(subject);
            subject.subscribe(propagator);
        }
        
        /**
         * Remove subject source.
         */
        public void remove(Subject subject) {
            subjects.remove(subject);
            subject.unsubscribe(propagator);
        }
        
    }
    
}

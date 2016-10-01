package org.cwi.examine.internal.layout.dwyer.cola;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Descent respects a collection of locks over nodes that should not move.
 */
public class Locks {
    Map<Integer, Point> locks;
    
    public Locks() {
        this.locks = new HashMap<Integer, Point>();
    }
    
    /**
     * Add a lock on the node at index id.
     * @method add
     * @param id index of node to be locked
     * @param p required position for node
     */
    public void add(int id, Point p) {
        this.locks.put(id, p);
    }
    
    /**
     * Clear all locks.
     */
    public void clear() {
        locks.clear();
    }
    
    /**
     * Whether locks exist.
     */
    public boolean isEmpty() {
        return locks.isEmpty();
    }
    
    /**
     * Perform an operation on each lock.
     */
    public void apply(LockOperation operation) {
        for (Entry<Integer, Point> entry: locks.entrySet()) {
            operation.apply(entry.getKey(), entry.getValue());
        }
    }
    
    public static interface LockOperation {
        
        public void apply(int index, Point point);
        
    }
    
}

/*
export class Locks {
    locks: any = {};
    * 
    add(id: number, x: number[]) {
        if (isNaN(x[0]) || isNaN(x[1])) debugger;
        this.locks[id] = x;
    }
    
    clear() {
        this.locks = {};
    }
    
    isEmpty(): boolean {
        for (var l in this.locks) return false;
        return true;
    }
    
    apply(f: (id: number, x: number[]) => void) {
        for (var l in this.locks) {
            f(l, this.locks[l]);
        }
    }
}*/
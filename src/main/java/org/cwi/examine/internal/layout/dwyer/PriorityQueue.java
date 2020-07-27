package org.cwi.examine.internal.layout.dwyer;

import java.util.Comparator;

/**
 *
 */
class PriorityQueue<T> {
    
    private Comparator<T> comparator;
    private PairingHeap<T> root;
    
    public PriorityQueue(Comparator<T> lessThan) {
        this.comparator = lessThan;
    }
    
    public T top() {
        return empty() ? null : root.min();
    }
    
    public PairingHeap<T> push(T... elements) {
        PairingHeap<T> pairingNode = null;
        
        for (T e: elements) {
            pairingNode = new PairingHeap(e);
            this.root = this.empty() ? pairingNode : root.merge(pairingNode, comparator);
        }
        
        return pairingNode;
    }
    
    public boolean empty() {
        return root == null || root.empty();
    }
    
    public T pop() {
        if (empty()) {
            return null;
        }
        
        T obj = root.min();
        root = root.removeMin(comparator);
        
        return obj;
    }
    
    public PairingHeap<T> reduceKey(PairingHeap<T> heapNode, T newKey) {
        PairingHeap.DecreaseKeyResult<T> r = this.root.decreaseKey(heapNode, newKey, this.comparator);
        this.root = r.root;
        
        return r.newNode;
    }
    
    /*
    public String toString(selector) {
        return this.root.toString(selector);
    }*/
    
}
package org.cwi.examine.internal.layout.dwyer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * From: https://gist.github.com/nervoussystem
 */
public class PairingHeap<T> {
    
    private T elem;
    private List<PairingHeap<T>> subHeaps;
    
    public PairingHeap(T elem) {
        this.elem = elem;
        this.subHeaps = new ArrayList<PairingHeap<T>>();
    }

    /*public String toString(selector) {
        String str = "";
        boolean needComma = false;
        
        for (var i = 0; i < this.subheaps.length; ++i) {
            var subheap = this.subheaps[i];
            if (!subheap.elem) {
                needComma = false;
                continue;
            }
            if (needComma) {
                str = str + ",";
            }
            str = str + subheap.toString(selector);
            needComma = true;
        }
        if (str !== "") {
            str = "(" + str + ")";
        }
        
        return (this.elem ? selector(this.elem) : "") + str;
    }*/

    public T min() {
        return elem;
    }

    public boolean empty() {
        return elem == null;
    }

    public PairingHeap<T> insert(T element, Comparator<T> comparator) {
        return merge(new PairingHeap<T>(element), comparator);
    }

    public PairingHeap<T> merge(PairingHeap<T> otherHeap, Comparator<T> comparator) {
        if (empty()) return otherHeap;
        else if (otherHeap.empty()) return this;
        else if (comparator.compare(this.elem, otherHeap.elem) < 0) {
            this.subHeaps.add(otherHeap);
            return this;
        } else {
            otherHeap.subHeaps.add(this);
            return otherHeap;
        }
    }

    public PairingHeap<T> removeMin(Comparator<T> comparator) {
        return empty() ? null : mergePairs(comparator);
    }

    public PairingHeap<T> mergePairs(Comparator<T> comparator) {
        if (subHeaps.isEmpty()) return new PairingHeap<T>(null);
        else if (subHeaps.size() == 1) return subHeaps.get(0);
        else {
            PairingHeap<T> firstPair = subHeaps.remove(subHeaps.size() - 1)
                                               .merge(subHeaps.remove(subHeaps.size() - 1), comparator);
            PairingHeap<T> remaining = mergePairs(comparator);
            
            return firstPair.merge(remaining, comparator);
        }
    }
    
    public DecreaseKeyResult<T> decreaseKey(PairingHeap<T> subHeap, T newValue, Comparator<T> comparator) {
        PairingHeap<T> newHeap = subHeap.removeMin(comparator);
        
        // Reassign subheap values to preserve tree.
        subHeap.elem = newHeap.elem;
        subHeap.subHeaps = newHeap.subHeaps;
        PairingHeap<T> pairingNode = new PairingHeap<T>(newValue);
        PairingHeap<T> heap = this.merge(pairingNode, comparator);
        
        return new DecreaseKeyResult<T>(heap, pairingNode);
    }
    
    
    public static class DecreaseKeyResult<T> {
        public final PairingHeap<T> root, newNode;

        public DecreaseKeyResult(PairingHeap<T> root, PairingHeap<T> newNode) {
            this.root = root;
            this.newNode = newNode;
        }
        
    }
    
}
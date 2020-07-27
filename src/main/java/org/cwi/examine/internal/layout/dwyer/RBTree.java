package org.cwi.examine.internal.layout.dwyer;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class RBTree<T> {
    private SortedSet<T> sortedSet;
    
    public RBTree(final Comparator<T> comparator) {
        // Hi-jack comparator to make items reference-unique.
        Comparator<T> uniqueComparator = new Comparator<T>() {

            @Override
            public int compare(T l, T r) {
                int result = comparator.compare(l, r);
                
                if(result == 0) {
                    result = l.hashCode() - r.hashCode();
                }
                
                return result;
            }
            
        };
        
        this.sortedSet = new TreeSet(uniqueComparator);
    }
    
    public boolean insert(T data) {
        return sortedSet.add(data);
    }
    
    public boolean remove(T data) {
        return sortedSet.remove(data);
    }
    
    /*public Iterator<T> findIter(T dataSet) {
        sortedSet.
    }
    findIter(dataSet: T): Iterator<T>;
    iterator(): Iterator<T>;
    
    public int size() {
        return sortedSet.size();
    }
    
    public static interface Iterator<T> {
        public T next();
        public T prev();
    }*/
    
}
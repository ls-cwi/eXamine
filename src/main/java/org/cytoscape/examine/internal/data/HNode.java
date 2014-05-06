package org.cytoscape.examine.internal.data;

import java.util.HashSet;
import java.util.Set;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

/**
 * Represents a node of interest.
 */
public class HNode extends HElement {
    
    // CyNode.
    public final CyNode cyNode;
    
    // CyRow.
    public final CyRow cyRow;
    
    // Unique identifier.
    public final String id;
    
    // Score.
    public final double score;
    
    // Sets that protein is associated with.
    public final Set<HSet> sets;
    
    /**
     * Base constructor, bare data.
     */
    public HNode(CyNode cyNode,
                 CyRow cyRow,
                 String id,
                 String name,
                 String url,
                 double score) {
        super(name, url);
        
        this.id = id;
        this.score = score;
        this.cyNode = cyNode;
        this.cyRow = cyRow;
        this.sets = new HashSet<HSet>();
    }

    @Override
    public String toString() {
        return name;
    }    
    
}

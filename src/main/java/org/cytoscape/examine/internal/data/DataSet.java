package org.cytoscape.examine.internal.data;

import org.cytoscape.examine.internal.signal.Variable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.examine.internal.Constants;
import org.cytoscape.examine.internal.Modules;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

/**
 * Data set / module.
 */
public class DataSet {
    
    // Entire network of interest.
    public final Variable<SuperNetwork> superNetwork;
    
    // CyNode to HNode map.
    public final Variable<Map<CyNode, HNode>> nodeMap;
    
    // Minimum and maximum node score (for normalization).
    public final Variable<Double> minScore, maxScore;
    
    // Node sets by category.
    public final Variable<Map<String, HCategory>> categories;
    
    // CySet to Hset map.
    public final Variable<Map<CyNode, HSet>> setMap;
    
    /**
     * Base constructor.
     */
    public DataSet() {
        this.superNetwork = new Variable<SuperNetwork>(new SuperNetwork(null, new Pseudograph<HNode, DefaultEdge>(DefaultEdge.class)));
        this.nodeMap = new Variable<Map<CyNode, HNode>>(new HashMap<CyNode, HNode>());
        this.minScore = new Variable<Double>(0.);
        this.maxScore = new Variable<Double>(1.);
        this.categories = new Variable<Map<String, HCategory>>(new HashMap<String, HCategory>());
        this.setMap = new Variable<Map<CyNode, HSet>>(new HashMap<CyNode, HSet>());
    }
    
    /**
     * Load data set from files.
     */
    public void load(CyNetwork cyNetwork, 
    		final String labelColumnName,
    		final String urlColumnName, 
    		final String scoreColumnName, 
    		final List<String> groupColumnNames, 
    		final List<Integer> groupColumnSizes,
    		CyGroupManager groupManager) {
    	
        // Important variables.
        List<CyNode> regularNodes = new ArrayList<CyNode>();// cyNetwork.getNodeList();
        CyTable nodeTable = cyNetwork.getDefaultNodeTable();
        
        // Extract nodes to visualize
        Collection<CyRow> selectedRows = cyNetwork.getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true);
        for (CyRow row : selectedRows) {
        	CyNode node = cyNetwork.getNode(row.get(CyNetwork.SUID, Long.class));
        	if (node != null && !groupManager.isGroup(node, cyNetwork)) {
        		regularNodes.add(node);
        	}
        }
        
        // Regular and group node partition.
        List<CyNode> groupNodes = new ArrayList<CyNode>();
        for (CyGroup group: groupManager.getGroupSet(cyNetwork)) {
            groupNodes.add(group.getGroupNode());
        }
        
        // Wrap cyNetwork.
        UndirectedGraph<HNode, DefaultEdge> superGraph =
                new Pseudograph<HNode, DefaultEdge>(DefaultEdge.class);
        
        // Nodes.
        Map<CyNode, HNode> nM = new HashMap<CyNode, HNode>();
        for(CyNode cyNode: regularNodes) {
            CyRow row = nodeTable.getRow(cyNode.getSUID());
            
            if(row != null) {
                HNode hN = new HNode(
                            cyNode,
                            row,
                            row.get(CyNetwork.NAME, String.class),
                            row.get(labelColumnName, String.class),
                            row.get(urlColumnName, String.class),
                            row.get(scoreColumnName, Double.class).doubleValue());
                
                superGraph.addVertex(hN);
                nM.put(cyNode, hN);
            }
        }
        
        // Edges.
        for(CyEdge cyEdge: cyNetwork.getEdgeList()) {
            HNode sHN = nM.get(cyEdge.getSource());
            HNode tHN = nM.get(cyEdge.getTarget());
            
            // todo: only edges between selected nodes
            if (sHN != null && tHN != null)
            	superGraph.addEdge(sHN, tHN);
        }
        
        SuperNetwork sN = new SuperNetwork(cyNetwork, superGraph);
        superNetwork.set(sN);
        
        // Wrap CyGroups.
        Map<String, HCategory> cs = new HashMap<String, HCategory>();
        
        // Determine category groups.
        double minScr = 1;
        double maxScr = 0;
        Map<CyNode, HSet> sM = new HashMap<CyNode, HSet>();
        for(CyNode gN: groupNodes) {
            CyRow row = nodeTable.getRow(gN.getSUID());
            
            // For every category node.
            if(row != null && row.get(CyNetwork.NAME, String.class).startsWith(Constants.CATEGORY_PREFIX)) {
                CyGroup catG = groupManager.getGroup(gN, cyNetwork);
                String catName = row.get(CyNetwork.NAME, String.class).substring(Constants.CATEGORY_PREFIX.length());
                
                int idx = 0;
                boolean found = false;
                for (String groupName : groupColumnNames) {
                	if (catName.equals(groupName)) {
                		found = true;
                		break;
                	}
                	idx++;
                }
                
                if (!found) continue;
                
                System.out.println("Add category: " + catName);
                
                // Construct member sets.
                List<HSet> members = new ArrayList<HSet>();
                for(CyNode mCN: catG.getNodeList()) {
                    CyRow mRow = nodeTable.getRow(mCN.getSUID());
                    
                    CyGroup mG = groupManager.getGroup(mCN, cyNetwork);
                    ArrayList<CyNode> mGNodes = new ArrayList<CyNode>(mG.getNodeList());
                    mGNodes.retainAll(regularNodes);
                    
                    // don't show sets with no nodes
                    if (mGNodes.size() == 0) continue;
                    
                    // Try to get symbol name, or fall back to id.
                    String name = mRow.get(labelColumnName, String.class);
                    if(name == null || name.trim().isEmpty()) {
                        name = mRow.get(CyNetwork.NAME, String.class);
                    }
                    
                    // Mapped member nodes.
                    List<HNode> mmNS = new ArrayList<HNode>();
                    for(CyNode mmN: mGNodes) {
                        HNode mmHN = nM.get(mmN);
                        if (mmHN != null)
                        	mmNS.add(mmHN);
                    }
                    
                    Double bScore = mRow.get(scoreColumnName, Double.class);
                    double score = bScore == null ? Double.NaN : bScore.doubleValue();
                    
                    if(!Double.isNaN(score)) {
                        minScr = Math.min(minScr, score);
                        maxScr = Math.max(maxScr, score);
                    }
                    
                    String url = mRow.get(urlColumnName, String.class);
                    
                    HSet mHS = new HSet(mG, name, score, url, mmNS);
                    members.add(mHS);
                    sM.put(mCN, mHS);
                    
                    // Register set with its members.
                    for(HNode mHN: mmNS) {
                        mHN.sets.add(mHS);
                    }
                }
            
                // Add category node.
                HCategory hC = new HCategory(catG, catName, members, groupColumnSizes.get(idx));
                
                cs.put(catName, hC);
            } // End for every category node.
            
            // Transfer node scores.
            this.minScore.set(minScr);
            this.maxScore.set(maxScr);
        }
        
        superNetwork.set(sN);
        nodeMap.set(nM);
        categories.set(cs);
                                
        // Clear active sets in model.
        Modules.model.selection.clear();
    }
    
}

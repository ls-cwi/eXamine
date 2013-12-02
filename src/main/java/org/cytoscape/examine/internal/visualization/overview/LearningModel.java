package org.cytoscape.examine.internal.visualization.overview;

import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.data.Network;
import org.cytoscape.examine.internal.model.Selection;
import org.cytoscape.examine.internal.visualization.Parameters;
import org.cytoscape.examine.internal.visualization.Parameters.TopologyEncoding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

/**
 * SOM learning samples.
 */
public class LearningModel {
    
    // Encoded proteins.
    public final List<HNode> proteins;
    
    // Encoded protein sets and weights.
    public final List<HSet> proteinSets;
    public final Map<HSet, Float> proteinSetWeights;
    
    // Induced protein sets.
    public final List<Set<HNode>> inducedProteinSets;

    // Features, according to protein list.
    public final float[][] features;

    // Protein to feature vector map.
    public final Map<HNode, float[]> vectorMap;
    
    // Feature vector size.
    public final int size, topologySetSize;

    /**
     * Base constructor.
     */
    public LearningModel(List<HNode> proteins,
                         Network contextNetwork,
                         Selection selectedSets) {
        this.proteins = proteins;
        this.proteinSets = new ArrayList<HSet>();
        this.proteinSetWeights = new HashMap<HSet, Float>();
        
        // Selected sets.
        proteinSets.addAll(selectedSets.activeSetList);
        for(HSet pS: selectedSets.activeSetList) {
            proteinSetWeights.put(pS, selectedSets.activeSetMap.get(pS));
        }
        
        // Induced protein sets.
        inducedProteinSets = new ArrayList<Set<HNode>>();
        for(HSet pS: proteinSets) {
            Set<HNode> set = new HashSet<HNode>();
            set.addAll(pS.elements);
            set.retainAll(proteins);
            
            inducedProteinSets.add(set);
        }
        
        features = new float[proteins.size()][];
        vectorMap = new HashMap<HNode, float[]>();
        
        // Combine proteins by identical neighborhood.
        Map<HNode, Set<HNode>> proteinToNgh = new HashMap<HNode, Set<HNode>>();
        for(HNode p: proteins) {
            List<HNode> nghPs = Graphs.neighborListOf(contextNetwork.graph, p);
            Set<HNode> nghSet = new HashSet<HNode>();
            nghSet.addAll(nghPs);
            proteinToNgh.put(p, nghSet);
        }
        
        TopologyEncoding tE = Parameters.somTopologyEncoding.get();
        
        Graph<Set<HNode>, DefaultEdge> indexGraph =
                new Pseudograph<Set<HNode>, DefaultEdge>(DefaultEdge.class);
        if(tE == TopologyEncoding.Compact) {
            for(Set<HNode> nghS: proteinToNgh.values()) {
                indexGraph.addVertex(nghS);
            }
            for(DefaultEdge e: contextNetwork.graph.edgeSet()) {
                HNode source = contextNetwork.graph.getEdgeSource(e);
                HNode target = contextNetwork.graph.getEdgeTarget(e);

                indexGraph.addEdge(proteinToNgh.get(source), proteinToNgh.get(target));
            }
        }
        // Determine number of topology encoding sets.
        switch(tE) {
            case Interaction:   topologySetSize = contextNetwork.graph.edgeSet().size();
                                break;
            case Compact:       topologySetSize = indexGraph.edgeSet().size();
                                break;
            case Neighborhood:  topologySetSize = proteins.size();
                                break;
            default:            topologySetSize = 0;
        }
        
        // Final size.
        this.size = proteinSets.size() + topologySetSize;
        
        // Per protein.
        for(int i = 0; i < proteins.size(); i++) {
            HNode protein = proteins.get(i);

            // Features for protein.
            float[] fs = new float[size];
            features[i] = fs;
            
            // Every protein set to a dimension,
            // where 0 => not in set, and 1 => in set.
            // Factor in set weight.
            for(int j = 0; j < proteinSets.size(); j++) {
                HSet pS = proteinSets.get(j);
                fs[j] = pS.elements.contains(protein) ? 1f : 0f;
                fs[j] *= proteinSetWeights.get(pS);
            }

            vectorMap.put(protein, features[i]);
        }
        
        // Quick protein index map.
        Map<HNode, Integer> proteinIndexMap = new HashMap<HNode, Integer>();
        for(int i = 0; i < proteins.size(); i++) {
            proteinIndexMap.put(proteins.get(i), i);
        }

        // Encode topology, where set weights are normalized to acc. to 1.
        float activated = 1f; // / (float) topologySetSize;
        
        if(tE == TopologyEncoding.Compact) {
            int i = 0;
            for(DefaultEdge e: indexGraph.edgeSet()) {
                Set<HNode> sNgh = indexGraph.getEdgeSource(e);
                Set<HNode> tNgh = indexGraph.getEdgeTarget(e);

                if(sNgh != tNgh) {
                    for(HNode source: sNgh) {
                        for(HNode target: tNgh) {
                            int sI = proteinIndexMap.get(source);
                            int tI = proteinIndexMap.get(target);
                            features[sI][proteinSets.size() + i] = activated;
                            features[tI][proteinSets.size() + i] = activated;
                        }
                    }

                    i++;
                }
            }
        }
        // Every interaction to a set of size 2.
        // where 0 => not in a neighborhood, and 1 => in a neighborhood.
        else if(tE == TopologyEncoding.Interaction) {
            int i = 0;
            for(DefaultEdge e: contextNetwork.graph.edgeSet()) {
                HNode source = contextNetwork.graph.getEdgeSource(e);
                HNode target = contextNetwork.graph.getEdgeTarget(e);
                
                if(source != target) {
                    int sI = proteinIndexMap.get(source);
                    int tI = proteinIndexMap.get(target);
                    features[sI][proteinSets.size() + i] = activated;
                    features[tI][proteinSets.size() + i] = activated;

                    i++;
                }
            }
        } else if(tE == TopologyEncoding.Neighborhood) {
            for(DefaultEdge e: contextNetwork.graph.edgeSet()) {
                HNode source = contextNetwork.graph.getEdgeSource(e);
                HNode target = contextNetwork.graph.getEdgeTarget(e);
                int sI = proteinIndexMap.get(source);
                int tI = proteinIndexMap.get(target);
                features[sI][proteinSets.size() + tI] = activated;
                features[tI][proteinSets.size() + sI] = activated;
            }
        }
    }

}
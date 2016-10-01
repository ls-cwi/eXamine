package org.cwi.examine.internal.model;

import org.cwi.examine.internal.data.*;
import org.cwi.examine.internal.signal.Observer;
import org.cwi.examine.internal.signal.Variable;
import org.cwi.examine.internal.signal.VolatileSet;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Model module. Manages enriched dataSet that includes
// information provided via user interaction.
public final class Model {
    private final DataSet dataSet;

    public final Selection selection;                               // Selected (visualized) annotations.
    public final VolatileSet<HCategory> openedCategories;           // Opened set categories.
    public final Variable<List<HCategory>> orderedCategories;
    public final VolatileSet<HNode> highlightedProteins;            // Highlighted proteins.
    public final VolatileSet<DefaultEdge> highlightedInteractions;  // Highlighted interactions.
    public final VolatileSet<HAnnotation> highlightedSets;          // Highlighted protein annotations.
    public final Variable<Network> activeNetwork;                   // Active network.

    public Model(final DataSet dataSet) {
        this.dataSet = dataSet;

        this.selection = new Selection(this);
        this.openedCategories = new VolatileSet<>();
        this.orderedCategories = new Variable<>(Collections.<HCategory>emptyList());
        this.highlightedProteins = new VolatileSet<>();
        this.highlightedInteractions = new VolatileSet<>();
        this.highlightedSets = new VolatileSet<>();
        this.activeNetwork = new Variable<>(new Network());

        // Update active network that is to be visualized.
        Observer activeNetworkObserver = () -> {
            Network superNetwork = dataSet.superNetwork.get();
            activeNetwork.set(superNetwork);
//            Set<HNode> moduleNodes = superNetwork.graph.vertexSet().stream()
//                    .filter(node -> node.score != 0.)
//                    .collect(Collectors.toSet());
//            Network module = new Network(Network.induce(moduleNodes, dataSet.superNetwork.get()));
//            activeNetwork.set(module);
        };

        //Parameters.visualStaticProteinBasis.change.subscribe(activeNetworkObserver);
        selection.change.subscribe(activeNetworkObserver);
        dataSet.superNetwork.change.subscribe(activeNetworkObserver);

        // Update ordered category list.
        Observer categoryObserver = () -> {
            List<HCategory> openedCat = new ArrayList<>();
            List<HCategory> closedCat = new ArrayList<>();
            for(HCategory c: dataSet.superNetwork.get().categories) {
                (openedCategories.get().contains(c) ? openedCat : closedCat).add(c);
            }

            openedCat.addAll(closedCat);
            orderedCategories.set(openedCat);
        };

        openedCategories.change.subscribe(categoryObserver);
        dataSet.superNetwork.change.subscribe(categoryObserver);
    }
}

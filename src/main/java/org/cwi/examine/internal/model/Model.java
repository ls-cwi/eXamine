package org.cwi.examine.internal.model;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.beans.property.*;
import org.cwi.examine.internal.data.*;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// Model module. Manages enriched dataSet that includes
// information provided via user interaction.
public final class Model {

    public final Selection selection;
    public final SetProperty<HCategory> openedCategories;
    public final ListProperty<HCategory> orderedCategories;
    public final SetProperty<HNode> highlightedProteins;
    public final SetProperty<DefaultEdge> highlightedInteractions;
    public final SetProperty<HAnnotation> highlightedSets;
    public final ObjectProperty<Network> activeNetwork;

    public Model(final DataSet dataSet) {
        this.selection = new Selection();
        this.openedCategories = new SimpleSetProperty<>(new ObservableSetWrapper<>(new HashSet<>()));
        this.orderedCategories = new SimpleListProperty<>(new ObservableListWrapper<>(new ArrayList<>()));
        this.highlightedProteins = new SimpleSetProperty<>(new ObservableSetWrapper<>(new HashSet<>()));
        this.highlightedInteractions = new SimpleSetProperty<>(new ObservableSetWrapper<>(new HashSet<>()));
        this.highlightedSets = new SimpleSetProperty<>(new ObservableSetWrapper<>(new HashSet<>()));
        this.activeNetwork = new SimpleObjectProperty<>(new Network());

        // Update active network that is to be visualized.
        dataSet.superNetwork.addListener((obs, old, categories) ->
                activeNetwork.set(dataSet.superNetwork.get()));

        // Update ordered category list.
        Runnable categoryObserver = () -> {
            List<HCategory> openedCat = new ArrayList<>();
            List<HCategory> closedCat = new ArrayList<>();
            for(HCategory c: dataSet.superNetwork.get().categories) {
                (openedCategories.contains(c) ? openedCat : closedCat).add(c);
            }

            openedCat.addAll(closedCat);
            orderedCategories.set(new ObservableListWrapper<>(openedCat));
        };

        openedCategories.addListener((obs, old, categories) -> categoryObserver.run());
        dataSet.superNetwork.addListener((obs, old, categories) -> categoryObserver.run());
    }
}

package org.cwi.examine.internal.data;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.cwi.examine.internal.data.domain.Annotation;
import org.cwi.examine.internal.data.domain.Node;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data set / module.
 */
public class DataSet {

    private static final String DATA_PATH = "data/";
    
    public final ObjectProperty<Network> superNetwork;

    public DataSet() {
        this.superNetwork = new SimpleObjectProperty<>(new Network());
    }

    /**
     * Load dataSet set from files.
     */
    private Network loadNetwork() throws FileNotFoundException {

        // Nodes.
        final Map<String, HNode> idToNode = new HashMap<>();
        for(final File file: resolveFiles(".nodes")) {
            loadNodes(file, idToNode);
        }
        final UndirectedGraph<HNode, DefaultEdge> superGraph = new Pseudograph<>(DefaultEdge.class);
        idToNode.values().forEach(node -> superGraph.addVertex(node));

        // Annotations and categories.
        final Map<String, HAnnotation> idToAnnotation = new HashMap<>();
        final Map<String, List<HAnnotation>> categoryToAnnotations = new HashMap<>();
        for(final File file: resolveFiles(".annotations")) {
            loadAnnotations(file, idToAnnotation, categoryToAnnotations);
        }

        // Categories.
        final List<HCategory> categories = new ArrayList<>();
        categoryToAnnotations.forEach((id, hAnnotations) ->
                categories.add(new HCategory(id, hAnnotations)));

        // Links, for both node <-> node and node <-> annotation.
        for(final File file: resolveFiles(".links")) {
            loadLinks(file, idToNode, idToAnnotation, superGraph);
        }

        return new Network(superGraph, categories);
    }

    private void loadNodes(final File file, final Map<String, HNode> idToNode)
            throws FileNotFoundException {

        final Map<String, String> nodeColumns = new HashMap<>();
        nodeColumns.put("Identifier", "identifier");
        nodeColumns.put("Symbol", "name");
        nodeColumns.put("URL", "url");
        nodeColumns.put("Score", "logFC");

        final List<Node> nodeBeans = csvToBean(file, Node.class, nodeColumns);
        //nodes.forEach(System.out::println);

        final List<HNode> graphNodes = nodeBeans.stream()
                .map(node -> new HNode(node.getIdentifier(), node.getName(), node.getUrl(), node.getScore()))
                .collect(Collectors.toList());

        mapIdToElement(graphNodes, idToNode);
    }

    private void loadAnnotations(final File file,
                                 final Map<String, HAnnotation> idToAnnotation,
                                 final Map<String, List<HAnnotation>> categoryToAnnotations)
        throws FileNotFoundException {

        final Map<String, String> annotationColumns = new HashMap<>();
        annotationColumns.put("Identifier", "identifier");
        annotationColumns.put("Symbol", "name");
        annotationColumns.put("URL", "url");
        annotationColumns.put("Score", "score");
        annotationColumns.put("Category", "category");

        final List<Annotation> annotations = csvToBean(file, Annotation.class, annotationColumns);
        //annotations.forEach(System.out::println);

        // Category <-> annotations.
        annotations.forEach(annotation -> {
            final HAnnotation hAnnotation = new HAnnotation(
                    annotation.getIdentifier(),
                    annotation.getName(),
                    annotation.getUrl(),
                    annotation.getScore());
            final String category = annotation.getCategory();

            idToAnnotation.put(annotation.getIdentifier(), hAnnotation);
            categoryToAnnotations
                    .computeIfAbsent(category, k -> new ArrayList<>())
                    .add(hAnnotation);
        });
    }

    private void loadLinks(final File linkFile,
                           final Map<String, HNode> idToNode,
                           final Map<String, HAnnotation> idToAnnotation,
                           final UndirectedGraph<HNode, DefaultEdge> graph)
            throws FileNotFoundException {

        final CSVReader csvReader = new CSVReader(new FileReader(linkFile), '\t');
        csvReader.forEach(ids -> {
            final String sourceId = ids[0]; // First column is link source.
            final HNode sourceNode = idToNode.get(sourceId);
            final HAnnotation sourceAnnotation = idToAnnotation.get(sourceId);

            // Remaining columns are link targets; one link per target.
            for(int i = 1; i < ids.length; i++) {
                final String targetId = ids[i];
                final HNode targetNode = idToNode.get(targetId);
                final HAnnotation targetAnnotation = idToAnnotation.get(targetId);

                // Node -> node.
                if(sourceNode != null && targetNode != null) {
                    graph.addEdge(sourceNode, targetNode);
                }
                // Node -> annotation.
                else if(sourceNode != null && targetAnnotation != null) {
                    sourceNode.addAnnotation(targetAnnotation);
                    targetAnnotation.addMember(sourceNode);
                }
                // Annotation -> node.
                else if(sourceAnnotation != null && targetNode != null) {
                    sourceAnnotation.addMember(targetNode);
                    targetNode.addAnnotation(sourceAnnotation);
                }
                // Invalid link: annotation -> annotation, or unknown identifiers.
                else {
                    System.err.println("Invalid link: " + sourceId + " -> " + targetId);
                }
            }
        });
    }

    public void load() throws IOException {
        superNetwork.set(loadNetwork());
    }

    private <T extends HElement> void mapIdToElement(final List<T> elements, Map<String, T> idToElement) {
        elements.forEach(e -> idToElement.put(e.identifier, e));
    }

    private List<File> resolveFiles(final String postFix) {
        final File dataRoot = new File(DATA_PATH);
        final File[] files = dataRoot.listFiles(file -> file.getName().endsWith(postFix));
        return Arrays.asList(files);
    }

    private <T> List<T> csvToBean(final File csvFile, final HeaderColumnNameMappingStrategy<T> strategy)
            throws FileNotFoundException {
        CsvToBean<T> csvToBean = new CsvToBean<>();

        CSVReader csvReader = new CSVReader(new FileReader(csvFile), '\t');

        return csvToBean.parse(strategy, csvReader);
    }

    private <T> List<T> csvToBean(final File csvFile, Class<T> classToMap, final Map<String, String> columnToBeanNames)
            throws FileNotFoundException {
        HeaderColumnNameTranslateMappingStrategy<T> strategy = new HeaderColumnNameTranslateMappingStrategy<>();
        strategy.setType(classToMap);
        strategy.setColumnMapping(columnToBeanNames);
        return csvToBean(csvFile, strategy);
    }

}

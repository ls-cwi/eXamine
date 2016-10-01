package org.cwi.examine.internal.data;

import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import org.cwi.examine.internal.data.domain.Annotation;
import org.cwi.examine.internal.data.domain.Link;
import org.cwi.examine.internal.data.domain.Node;
import org.cwi.examine.internal.signal.Variable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

/**
 * Data set / module.
 */
public class DataSet {
    
    public final Variable<Network> superNetwork;

    public DataSet() {
        this.superNetwork = new Variable<>(new Network());
    }

    public <T extends HElement> Map<String, T> mapIdToElement(List<T> elements) {
        Map<String, T> map = new HashMap<>();
        elements.forEach(e -> map.put(e.identifier, e));
        return map;
    }

    /**
     * Load dataSet set from files.
     */
    public Network beansToNetwork(final List<Node> nodes, final List<Link> links, final List<Annotation> annotations) {
        final List<HNode> graphNodes = nodes.stream()
                .filter(node -> !node.getModule().isEmpty())
                .map(node -> new HNode(node.getIdentifier(), node.getName(), node.getUrl(), node.getScore()))
                .collect(Collectors.toList());
        final Map<String, HNode> idToGraphNode = mapIdToElement(graphNodes);

        // Nodes.
        final UndirectedGraph<HNode, DefaultEdge> superGraph = new Pseudograph<>(DefaultEdge.class);
        graphNodes.forEach(superGraph::addVertex);

        // Node <-> Node.
        links.forEach(link -> {
            if(idToGraphNode.containsKey(link.getSource()) && idToGraphNode.containsKey(link.getTarget())) {
                superGraph.addEdge(idToGraphNode.get(link.getSource()), idToGraphNode.get(link.getTarget()));
            }
        });

        // Annotations.
        final List<HAnnotation> sets = annotations.stream().map(a ->
                new HAnnotation(a.getIdentifier(), a.getName(), a.getScore(), a.getUrl())).collect(Collectors.toList());
        final Map<String, HAnnotation> idToAnnotation = mapIdToElement(sets);

        // Annotation <-> Node.
        nodes.forEach(node -> {
            final HNode hNode = idToGraphNode.get(node.getIdentifier());
            final List<String> nodeAnnotationStrings = Arrays.asList(
                    node.getComponents(), node.getFunctions(), node.getPathways(), node.getProcesses());
            nodeAnnotationStrings.forEach(annotationIds -> {
                for(final String annotationId: annotationIds.split("\\|")) {
                    final HAnnotation annotation = idToAnnotation.get(annotationId);
                    if(annotation == null) {
                        System.out.println("Unknown annotation identifier: " + annotationId);
                    } else {
                        annotation.addMember(hNode);
                        hNode.addAnnotation(annotation);
                    }
                }
            });
        });

        // Category <-> annotations.
        final Map<String, List<HAnnotation>> categoryToAnnotations = new HashMap<>();
        categoryToAnnotations.put("Process", new ArrayList<>());
        categoryToAnnotations.put("Function", new ArrayList<>());
        categoryToAnnotations.put("Component", new ArrayList<>());
        categoryToAnnotations.put("Pathway", new ArrayList<>());

        annotations.forEach(annotation -> {
            final HAnnotation hAnnotation = idToAnnotation.get(annotation.getIdentifier());
            final String category = annotation.getCategory();
            categoryToAnnotations.get(category).add(hAnnotation);
        });

        // Categories.
        final List<HCategory> categories = new ArrayList<>();
        categoryToAnnotations.forEach((id, hAnnotations) -> categories.add(new HCategory(id, hAnnotations)));

        return new Network(superGraph, categories);
    }

    /**
     * Load data set from files.
     */
    public void load() throws FileNotFoundException {
        final File nodeFile = resolveFile("nodes.tsv");
        final Map<String, String> nodeColumns = new HashMap<>();
        nodeColumns.put("ID", "identifier");
        nodeColumns.put("Symbol", "name");
        nodeColumns.put("URL", "url");
        nodeColumns.put("Module", "module");
        nodeColumns.put("Score", "logFC");
        nodeColumns.put("Process", "processes");
        nodeColumns.put("Function", "functions");
        nodeColumns.put("Component", "components");
        nodeColumns.put("Pathway", "pathways");
        final List<Node> nodes = csvToBean(nodeFile, Node.class, nodeColumns);
        //nodes.forEach(System.out::println);

        final File linkFile = resolveFile("links.tsv");
        final List<Link> links = csvToBean(linkFile, Link.class, "source", "target");
        //edges.forEach(System.out::println);

        final File annotationFile = resolveFile("annotations.tsv");
        final Map<String, String> annotationColumns = new HashMap<>();
        annotationColumns.put("ID", "identifier");
        annotationColumns.put("Symbol", "name");
        annotationColumns.put("URL", "url");
        annotationColumns.put("score", "score");
        annotationColumns.put("category", "category");
        final List<Annotation> annotations = csvToBean(annotationFile, Annotation.class, annotationColumns);
        //annotations.forEach(System.out::println);

        // Update model.
        superNetwork.set(beansToNetwork(nodes, links, annotations));
    }

    private File resolveFile(final String name) {
        return new File("data/" + name);
    }

    private <T> List<T> csvToBean(final File csvFile, final HeaderColumnNameMappingStrategy<T> strategy)
            throws FileNotFoundException {
        CsvToBean<T> csvToBean = new CsvToBean<>();

        CSVReader csvReader = new CSVReader(new FileReader(csvFile), '\t');

        return csvToBean.parse(strategy, csvReader);
    }

    private <T> List<T> csvToBean(final File csvFile, Class<T> classToMap, final String... beanNames)
            throws FileNotFoundException {
        ColumnPositionMappingStrategy<T> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(classToMap);
        strategy.setColumnMapping(beanNames);
        return csvToBean(csvFile, strategy);
    }

    private <T> List<T> csvToBean(final File csvFile, Class<T> classToMap, final Map<String, String> columnToBeanNames)
            throws FileNotFoundException {
        HeaderColumnNameTranslateMappingStrategy<T> strategy = new HeaderColumnNameTranslateMappingStrategy<>();
        strategy.setType(classToMap);
        strategy.setColumnMapping(columnToBeanNames);
        return csvToBean(csvFile, strategy);
    }

}

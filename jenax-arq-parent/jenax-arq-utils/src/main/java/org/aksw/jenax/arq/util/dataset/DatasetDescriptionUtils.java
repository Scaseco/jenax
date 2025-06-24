package org.aksw.jenax.arq.util.dataset;

import java.util.Collection;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Quad;

import com.google.common.base.Joiner;

public class DatasetDescriptionUtils {

    /** Create method that treats null arguments as empty lists. */
    public static DatasetDescription ofStrings(Collection<String> graphIris, Collection<String> namedGraphIris) {
        return DatasetDescription.create(asListOrCopy(graphIris), asListOrCopy(namedGraphIris));
    }

    public static DatasetDescription ofNodes(Collection<Node> graphIris, Collection<Node> namedGraphIris) {
        return DatasetDescription.create(
            asListOrCopy(graphIris).stream().map(Node::getURI).toList(),
            asListOrCopy(namedGraphIris).stream().map(Node::getURI).toList());
    }

    // Move to CollectionUtils / ListUtils
    private static <T> List<T> asListOrCopy(Collection<T> items) {
        return items == null
            ? List.of()
            : asListOrCopyOrNull(items);
    }

    // Move to CollectionUtils / ListUtils
    private static <T> List<T> asListOrCopyOrNull(Collection<T> items) {
        return items instanceof List<T> list
            ? list
            : items == null
                ? null
                : List.copyOf(items);
    }

    /**
     * Add all default- and named graph iris from source to target
     * TODO Prevent duplicates
     */
    public static void mergeInto(DatasetDescription target, DatasetDescription source) {
        target.addAllDefaultGraphURIs(source.getDefaultGraphURIs());
        target.addAllNamedGraphURIs(source.getNamedGraphURIs());
    }


    /**
     * If the argument is null or there is only one default graph, this graph IRI is returned; otherwise null.
     *
     * @param datasetDescription
     * @return
     */
    public static String getSingleDefaultGraphUri(DatasetDescription datasetDescription) {
        String result;

        if(datasetDescription == null) {
            result = Quad.defaultGraphIRI.getURI();
        } else {

            List<String> dgus = datasetDescription.getDefaultGraphURIs();

            result = datasetDescription != null && dgus.size() == 1
                    ? dgus.iterator().next()
                    : null
                    ;
        }

        return result;
    }


    public static DatasetDescription createDefaultGraph(Node defaultGraph) {
        DatasetDescription result = createDefaultGraph(defaultGraph.getURI());
        return result;
    }

    public static DatasetDescription createDefaultGraph(String defaultGraph) {
        DatasetDescription result = new DatasetDescription();
        result.addDefaultGraphURI(defaultGraph);
        return result;
    }

    public static String toString(DatasetDescription datasetDescription) {
        String result = datasetDescription == null
            ? null
            : "[defaultGraphs = " + Joiner.on(", ").join(datasetDescription.getDefaultGraphURIs()) + "]"
            + "[namedGraphs = " + Joiner.on(", ").join(datasetDescription.getNamedGraphURIs()) + "]";

        return result;
    }

}

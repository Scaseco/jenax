package org.aksw.jenax.arq.util.triple;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.quad.DatasetUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFFormat;

/**
 * @author Claus Stadler
 */
public class ModelUtils {
    /** Create a union model of all unique non-null arguments */
    // XXX Order by size?
    public static Model union(Model ...models) {
        Model result = Stream.of(models)
                .filter(Objects::nonNull)
                .distinct()
                .reduce(ModelFactory::createUnion)
                .orElse(ModelFactory.createDefaultModel()); // Empty model?
        return result;
    }

    public static Stream<Node> streamNodes(Model model) {
        return GraphUtils.streamNodes(model.getGraph());
    }

    public static Iterator<Node> iterateNodes(Model model) {
        return GraphUtils.iterateNodes(model.getGraph());
    }

    /**
     * Remove all unused prefixes form the given model's prefix mapping.
     * Assumes that the model delegates to the prefix mapping of an underlying {@link Graph}.
     *
     * @param graph The model whose prefix mapping to optimize
     * @return The given model
     */
    public static Model optimizePrefixes(Model model) {
        GraphUtils.optimizePrefixes(model.getGraph());
        return model;
    }

    public static String toString(Model model, RDFFormat rdfFormat) {
        return DatasetUtils.toString(DatasetFactory.wrap(model), rdfFormat);
    }
}

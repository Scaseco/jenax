package org.aksw.jenax.arq.util.triple;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;

/**
 * @author Claus Stadler
 */
public class ModelUtils {

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

}

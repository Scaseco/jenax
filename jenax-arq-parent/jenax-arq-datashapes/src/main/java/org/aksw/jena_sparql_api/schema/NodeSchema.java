package org.aksw.jena_sparql_api.schema;

import java.util.Collection;
import java.util.Set;

import org.aksw.jenax.arq.util.triple.TripleFilter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

public interface NodeSchema {
    PropertySchema createPropertySchema(Node predicate, boolean isForward);

    Set<TripleFilter> getGenericPatterns();
    Collection<? extends PropertySchema> getPredicateSchemas();
//    NodeGraphView instantiate(Node node);

    /**
     * Copy triples that match the predicate specification from the source graph into
     * the target graph.
     *
     * @param target
     * @param source
     */
    default long copyMatchingTriples(Node source, Graph targetGraph, Graph sourceGraph) {
        long result = 0;

        // TODO Handle the generic patterns

        for (PropertySchema predicateSchema : getPredicateSchemas()) {
            long contrib = predicateSchema.copyMatchingTriples(source, targetGraph, sourceGraph);
            result += contrib;
        }

        return result;
    }
}

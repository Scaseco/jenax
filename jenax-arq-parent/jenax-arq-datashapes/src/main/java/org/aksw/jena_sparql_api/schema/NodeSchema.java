package org.aksw.jena_sparql_api.schema;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.triple.TripleFilter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public interface NodeSchema {
    PropertySchema createPropertySchema(Node predicate, boolean isForward);

    Set<TripleFilter> getGenericPatterns();
    Collection<? extends PropertySchema> getPredicateSchemas();
//    NodeGraphView instantiate(Node node);


    default Stream<Triple> streamMatchingTriples(Node source, Graph sourceGraph) {
        return getPredicateSchemas().stream().flatMap(ps -> ps.streamMatchingTriples(source, sourceGraph));
    }

    /**
     * Copy triples that match the predicate specification from the source graph into
     * the target graph.
     *
     * @param target
     * @param source
     */
    // XXX Perhaps move to util function?
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

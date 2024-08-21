package org.aksw.jenax.graphql.sparql.v2.api.low;

public interface GraphQlFieldProcessor<K> {
    /** The name of the field for which this is the processor. {@code null} for the query itself. Experimental. */
    String getName();
    GraphQlFieldExecBuilder<K> newExecBuilder();
}

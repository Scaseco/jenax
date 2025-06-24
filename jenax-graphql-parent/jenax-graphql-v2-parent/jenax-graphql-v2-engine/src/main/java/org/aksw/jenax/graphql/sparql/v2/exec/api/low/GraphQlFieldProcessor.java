package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

public interface GraphQlFieldProcessor<K> {
    /** Get the processor for the overall document from which the field processor was derived. */
    GraphQlProcessor<K> getGraphQlProcessor();

    /** The name of the field for which this is the processor. {@code null} for the query itself. Experimental. */
    String getName();
    GraphQlFieldExecBuilder<K> newExecBuilder();
}

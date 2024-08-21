package org.aksw.jenax.graphql.sparql.v2.api.low;

public interface RdfGraphQlProcessorFactory<K> {
    RdfGraphQlProcessorBuilder<K> newBuilder();
}

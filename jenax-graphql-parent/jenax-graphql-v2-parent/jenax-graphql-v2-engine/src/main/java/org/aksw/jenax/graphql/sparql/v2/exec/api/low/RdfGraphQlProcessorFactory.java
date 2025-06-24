package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

public interface RdfGraphQlProcessorFactory<K> {
    RdfGraphQlProcessorBuilder<K> newBuilder();
}

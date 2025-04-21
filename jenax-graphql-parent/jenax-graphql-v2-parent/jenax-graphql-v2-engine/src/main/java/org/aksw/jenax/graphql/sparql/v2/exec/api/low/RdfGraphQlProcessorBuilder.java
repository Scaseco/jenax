package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

public interface RdfGraphQlProcessorBuilder<K>
    extends GraphQlProcessorSettings<RdfGraphQlProcessorBuilder<K>>
{
    GraphQlProcessor<K> build();
}

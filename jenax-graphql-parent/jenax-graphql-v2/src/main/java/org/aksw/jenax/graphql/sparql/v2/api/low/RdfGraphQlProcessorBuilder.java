package org.aksw.jenax.graphql.sparql.v2.api.low;

public interface RdfGraphQlProcessorBuilder<K>
    extends GraphQlProcessorSettings<RdfGraphQlProcessorBuilder<K>>
{
    GraphQlProcessor<K> build();
}

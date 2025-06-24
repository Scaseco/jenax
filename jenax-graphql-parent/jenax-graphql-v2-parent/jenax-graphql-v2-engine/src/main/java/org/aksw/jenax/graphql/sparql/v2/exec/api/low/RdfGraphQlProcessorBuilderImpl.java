package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

import org.aksw.jenax.graphql.sparql.v2.rewrite.GraphQlToSparqlConverterBase;

public class RdfGraphQlProcessorBuilderImpl<K>
    extends RdfGraphQlProcessorBuilderBase<K>
{
    public RdfGraphQlProcessorBuilderImpl(GraphQlToSparqlConverterBuilder<K> converterBuilder) {
        super(converterBuilder);
    }

    // protected Creator<? extends GraphQlToSparqlConverterBuilder<K>> converterBuilder;

//    public RdfGraphQlProcessorBuilderImpl(Creator<? extends GraphQlToSparqlConverterBuilder<K>> converterBuilder) {
//        super();
//        this.converterBuilder = Objects.requireNonNull(converterBuilder);
//    }

    @Override
    public GraphQlProcessor<K> build() {
        GraphQlToSparqlConverterBase<K> converter = converterBuilder.build();
        return GraphQlProcessor.of(document, assignments, converter);
    }
}

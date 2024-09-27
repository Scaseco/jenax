package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.rewrite.GraphQlToSparqlConverterBase;
import org.apache.jena.atlas.lib.Creator;

public class RdfGraphQlProcessorBuilderImpl<K>
    extends RdfGraphQlProcessorBuilderBase<K>
{
    protected Creator<? extends GraphQlToSparqlConverterBase<K>> converterFactory;

    public RdfGraphQlProcessorBuilderImpl(Creator<? extends GraphQlToSparqlConverterBase<K>> converterFactory) {
        super();
        this.converterFactory = Objects.requireNonNull(converterFactory);
    }

    @Override
    public GraphQlProcessor<K> build() {
        return GraphQlProcessor.of(document, assignments, converterFactory);
    }
}

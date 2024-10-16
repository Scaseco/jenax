package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.rewrite.GraphQlToSparqlConverterBase;
import org.aksw.jenax.graphql.sparql.v2.rewrite.GraphQlToSparqlConverterJson;
import org.aksw.jenax.graphql.sparql.v2.rewrite.GraphQlToSparqlConverterRon;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.sparql.path.P_Path0;

public class RdfGraphQlProcessorFactoryImpl<K>
    implements RdfGraphQlProcessorFactory<K>
{
    protected Creator<? extends GraphQlToSparqlConverterBase<K>> converterFactory;

    public RdfGraphQlProcessorFactoryImpl(Creator<? extends GraphQlToSparqlConverterBase<K>> converterFactory) {
        super();
        this.converterFactory = Objects.requireNonNull(converterFactory);
    }

    @Override
    public RdfGraphQlProcessorBuilder<K> newBuilder() {
        return new RdfGraphQlProcessorBuilderImpl<>(converterFactory);
    }

    public static RdfGraphQlProcessorFactory<String> forJson() {
        return new RdfGraphQlProcessorFactoryImpl<>(GraphQlToSparqlConverterJson::new);
    }

    public static RdfGraphQlProcessorFactory<P_Path0> forRon() {
        return new RdfGraphQlProcessorFactoryImpl<>(GraphQlToSparqlConverterRon::new);
    }
}

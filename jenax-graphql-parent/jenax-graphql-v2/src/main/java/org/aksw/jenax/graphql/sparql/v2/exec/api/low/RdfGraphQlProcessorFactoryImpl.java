package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

import java.util.Objects;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.sparql.path.P_Path0;

public class RdfGraphQlProcessorFactoryImpl<K>
    implements RdfGraphQlProcessorFactory<K>
{
    protected Creator<? extends GraphQlToSparqlConverterBuilder<K>> converterBuilderCreator;

    public RdfGraphQlProcessorFactoryImpl(Creator<? extends GraphQlToSparqlConverterBuilder<K>> converterBuilderCreator) {
        super();
        this.converterBuilderCreator = Objects.requireNonNull(converterBuilderCreator);
    }

    @Override
    public RdfGraphQlProcessorBuilder<K> newBuilder() {
        return new RdfGraphQlProcessorBuilderImpl<>(converterBuilderCreator.create());
    }

    public static RdfGraphQlProcessorFactory<String> forJson() {
        return new RdfGraphQlProcessorFactoryImpl<>(() -> GraphQlToSparqlConverterBuilder.forJson());
    }

    public static RdfGraphQlProcessorFactory<P_Path0> forRon() {
        return new RdfGraphQlProcessorFactoryImpl<>(() -> GraphQlToSparqlConverterBuilder.forRon());
    }
}

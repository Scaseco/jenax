package org.aksw.jenax.graphql.sparql;

import java.util.Objects;

public class GraphQlToSparqlMappingBuilderImpl
    extends GraphQlToSparqlMappingBuilderBase
{
    @Override
    public GraphQlToSparqlMapping build() {
        Objects.requireNonNull(resolver);
        Objects.requireNonNull(document);
        return new GraphQlToSparqlConverter(resolver, jsonMode).convertDocument(document, assignments);
    }
}

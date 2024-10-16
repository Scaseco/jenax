package org.aksw.jenax.graphql.sparql;

import java.util.Map;

import graphql.language.Document;
import graphql.language.Value;

public interface GraphQlToSparqlMappingBuilder {
    GraphQlToSparqlMappingBuilder setResolver(GraphQlResolver resolver);
    GraphQlToSparqlMappingBuilder setDocument(Document document);
    GraphQlToSparqlMappingBuilder setAssignments(Map<String, Value<?>> assignments);

    // XXX We probably need a better conversion mode flag - maybe an enum?
    GraphQlToSparqlMappingBuilder setJsonMode(boolean onOrOff);

    // XXX The result is not an interface but a concrete class; perhaps not ideal
    GraphQlToSparqlMapping build();
}

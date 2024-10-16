package org.aksw.jenax.graphql.sparql;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import graphql.language.Document;
import graphql.language.Value;

public abstract class GraphQlToSparqlMappingBuilderBase
    implements GraphQlToSparqlMappingBuilder
{
    protected GraphQlResolver resolver;
    protected boolean jsonMode;
    protected Document document;
    protected Map<String, Value<?>>  assignments;

    public GraphQlToSparqlMappingBuilderBase() {
        super();
        this.assignments = new LinkedHashMap<>();
    }

    @Override
    public GraphQlToSparqlMappingBuilder setResolver(GraphQlResolver resolver) {
        this.resolver = resolver;
        return this;
    }

    @Override
    public GraphQlToSparqlMappingBuilder setJsonMode(boolean jsonMode) {
        this.jsonMode = jsonMode;
        return this;
    }

    @Override
    public GraphQlToSparqlMappingBuilder setDocument(Document document) {
        Objects.requireNonNull(document);
        this.document = document;
        return this;
    }

    @Override
    public GraphQlToSparqlMappingBuilder setAssignments(Map<String, Value<?>>  assignments) {
        if (assignments != null) {
            this.assignments.putAll(assignments);
        }
        return this;
    }
}

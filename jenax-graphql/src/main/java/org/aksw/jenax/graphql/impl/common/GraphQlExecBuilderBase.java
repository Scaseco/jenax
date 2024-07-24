package org.aksw.jenax.graphql.impl.common;

import java.util.Map;
import java.util.Objects;

import org.aksw.jenax.graphql.json.api.GraphQlExecBuilder;

import graphql.language.Document;
import graphql.language.Value;

public abstract class GraphQlExecBuilderBase
    implements GraphQlExecBuilder
{
    protected Document document;
    protected String documentString;
    protected Map<String, Value<?>> assignments;

    @Override
    public GraphQlExecBuilder setDocument(Document document) {
        Objects.requireNonNull(document);
        this.document = document;
        this.documentString = null;
        return this;
    }

    @Override
    public GraphQlExecBuilder setDocument(String documentString) {
        Objects.requireNonNull(documentString);
        this.document = null;
        this.documentString = documentString;
        return this;
    }

    @Override
    public GraphQlExecBuilder setVar(String varName, Value<?> value) {
        assignments.put(varName, value);
        return this;
    }

    @Override
    public GraphQlExecBuilder setAssignments(Map<String, Value<?>> assignments) {
        assignments.putAll(assignments);
        return this;
    }
}

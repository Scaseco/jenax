package org.aksw.jenax.graphql.json.api;

import java.util.Map;

import graphql.language.Document;
import graphql.language.Value;

public interface GraphQlExecBuilder {
    GraphQlExecBuilder setDocument(Document document);
    GraphQlExecBuilder setDocument(String documentString);

    // GraphQlExecBuilder setVar(String varName, Node value);
    GraphQlExecBuilder setVar(String varName, Value<?> value);
    GraphQlExecBuilder setAssignments(Map<String, Value<?>> assignments);

    GraphQlExec build();
}

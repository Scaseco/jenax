package org.aksw.jenax.graphql.api;

import graphql.language.Document;

public interface GraphQlExecFactory {
    /**
     * A typical graphql JSON request has the attributes {@code { "query": "...", "variables": { ... } "operationName": "..." } }
     * This method accounts for the parsed value of the 'query' field.
     */
    GraphQlExec create(Document document);
}

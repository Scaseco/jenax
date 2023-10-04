package org.aksw.jenax.graphql;

import graphql.language.Document;

public interface GraphQlExecFactory {
    /**
     * A typical graphql request has the attributes { "query": "...", "variables": { ... } "operationName": "..." }
     * Right now we only account for the parsed value of the query field.
     */
    GraphQlExec create(Document document);
}

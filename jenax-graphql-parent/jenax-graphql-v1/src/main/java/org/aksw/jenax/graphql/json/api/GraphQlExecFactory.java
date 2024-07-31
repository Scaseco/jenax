package org.aksw.jenax.graphql.json.api;

import java.util.Map;

import graphql.language.Document;
import graphql.language.Value;

/**
 * Public high-level interface.
 * Typically implemented as a wrapper over {@link GraphQlExecFactoryDocument}.
 */
public interface GraphQlExecFactory
    extends GraphQlExecFactoryDocument, GraphQlExecFactoryString
{
    GraphQlExecBuilder newBuilder();

    @Override
    default GraphQlExec create(Document document, Map<String, Value<?>> assignments) {
        return newBuilder().setDocument(document).setAssignments(assignments).build();
    }

    @Override
    default GraphQlExec create(String documentString, Map<String, Value<?>> assignments) {
        return newBuilder().setDocument(documentString).setAssignments(assignments).build();
    }
}

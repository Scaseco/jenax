package org.aksw.jenax.graphql.impl.common;

import java.util.Map;

import org.aksw.jenax.graphql.json.api.GraphQlExec;
import org.aksw.jenax.graphql.json.api.GraphQlExecFactoryDocument;

import graphql.language.Document;
import graphql.language.Value;

public interface GraphQlExecFactoryDocumentWrapper
    extends GraphQlExecFactoryDocument
{
    GraphQlExecFactoryDocument getDelegate();

    default GraphQlExec create(Document document, Map<String, Value<?>> assignments) {
        return getDelegate().create(document, assignments);
    }
}

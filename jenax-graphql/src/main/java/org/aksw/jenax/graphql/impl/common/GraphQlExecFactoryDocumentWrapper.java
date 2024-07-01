package org.aksw.jenax.graphql.impl.common;

import org.aksw.jenax.graphql.api.GraphQlExec;
import org.aksw.jenax.graphql.api.GraphQlExecFactoryDocument;

import graphql.language.Document;

public interface GraphQlExecFactoryDocumentWrapper
    extends GraphQlExecFactoryDocument
{
    GraphQlExecFactoryDocument getDelegate();

    default GraphQlExec create(Document document) {
        return getDelegate().create(document);
    }
}

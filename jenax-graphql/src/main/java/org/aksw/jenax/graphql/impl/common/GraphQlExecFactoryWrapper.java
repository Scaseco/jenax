package org.aksw.jenax.graphql.impl.common;

import org.aksw.jenax.graphql.api.GraphQlExec;
import org.aksw.jenax.graphql.api.GraphQlExecFactory;

import graphql.language.Document;

public interface GraphQlExecFactoryWrapper
    extends GraphQlExecFactory
{
    GraphQlExecFactory getDelegate();

    @Override
    default GraphQlExec create(Document document) {
        return getDelegate().create(document);
    }
}

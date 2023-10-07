package org.aksw.jenax.graphql;

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

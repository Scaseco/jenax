package org.aksw.jenax.graphql.impl.common;

import org.aksw.jenax.graphql.json.api.GraphQlExecFactoryDocument;

public class GraphQlExecFactoryDocumentWrapperBase
    implements GraphQlExecFactoryDocumentWrapper
{
    protected GraphQlExecFactoryDocument delegate;

    public GraphQlExecFactoryDocumentWrapperBase(GraphQlExecFactoryDocument delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public GraphQlExecFactoryDocument getDelegate() {
        return delegate;
    }
}

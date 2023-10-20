package org.aksw.jenax.dataaccess.sparql.datasource;

public class RdfDataSourceWrapperBase
    implements RdfDataSourceWrapper
{
    protected RdfDataSource delegate;

    public RdfDataSourceWrapperBase(RdfDataSource delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public RdfDataSource getDelegate() {
        return delegate;
    }
}

package org.aksw.jenax.connection.datasource;

public class RdfDataSourceDelegateBase
    implements RdfDataSourceDelegate
{
    protected RdfDataSource delegate;

    public RdfDataSourceDelegateBase(RdfDataSource delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public RdfDataSource getDelegate() {
        return delegate;
    }
}

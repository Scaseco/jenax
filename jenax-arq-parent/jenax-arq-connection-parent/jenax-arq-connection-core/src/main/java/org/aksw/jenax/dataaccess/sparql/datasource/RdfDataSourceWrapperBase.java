package org.aksw.jenax.dataaccess.sparql.datasource;

public class RdfDataSourceWrapperBase<T extends RdfDataSource>
    implements RdfDataSourceWrapper<T>
{
    protected T delegate;

    public RdfDataSourceWrapperBase(T delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public T getDelegate() {
        return delegate;
    }
}

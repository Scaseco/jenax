package org.aksw.jenax.dataaccess.sparql.datasource;

public class RDFDataSourceWrapperBase<T extends RDFDataSource>
    implements RDFDataSourceWrapper<T>
{
    protected T delegate;

    public RDFDataSourceWrapperBase(T delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public T getDelegate() {
        return delegate;
    }
}

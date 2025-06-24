package org.aksw.jenax.dataaccess.sparql.engine;

import org.aksw.commons.util.closeable.AutoCloseableWrapperBase;

public class RDFEngineWrapperBase<X extends RDFEngine>
    extends AutoCloseableWrapperBase<X>
    implements RDFEngineWrapper<X>
{
    protected X delegate;

    public RDFEngineWrapperBase(X delegate) {
        super(delegate);
    }

    @Override
    public X getDelegate() {
        return delegate;
    }

//     @Override
//    public RDFLinkBuilder newLinkBuilder() {
//        X tmp = getDelegate();
//        return tmp.newLinkBuilder();
//    }
//
//    @Override
//    public DatasetGraph getDataset() {
//        X tmp = getDelegate();
//        return tmp.getDataset();
//    }

/*
    @Override
    public RDFDataSource newDefaultDataSource() {
        X tmp = getDelegate();
        return tmp.newDefaultDataSource();
    }
*/
}

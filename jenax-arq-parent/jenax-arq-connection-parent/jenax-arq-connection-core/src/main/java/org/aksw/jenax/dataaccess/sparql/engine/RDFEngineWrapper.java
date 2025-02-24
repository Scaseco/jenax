package org.aksw.jenax.dataaccess.sparql.engine;

import java.util.Optional;

import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;

public interface RDFEngineWrapper<X extends RDFEngine>
    extends RDFEngine
{
    X getDelegate();

    @Override
    default RDFLinkSource getLinkSource() {
        X delegate = getDelegate();
        return delegate.getLinkSource();
    }

    @Override
    default Optional<ServiceControl> getServiceControl() {
        X delegate = getDelegate();
        return delegate.getServiceControl();
    }

    @Override
    default void close() throws Exception {
        X delegate = getDelegate();
        if (delegate != null) {
            delegate.close();
        }
    }

//    @Override
//    default RDFLinkBuilder newLinkBuilder() {
//        X delegate = getDelegate();
//        return delegate.newLinkBuilder();
//    }

//    @Override
//    default RDFDataSource newDefaultDataSource() {
//        X delegate = getDelegate();
//        return delegate.newDefaultDataSource();
//    }
}

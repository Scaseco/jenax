package org.aksw.jenax.dataaccess.sparql.engine;

import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;

public interface RDFEngineWrapper<X extends RDFEngine>
    extends RDFEngine
{
    X getDelegate();

    @Override
    default RDFLinkBuilder newLinkBuilder() {
        X delegate = getDelegate();
        return delegate.newLinkBuilder();
    }

//    @Override
//    default RDFDataSource newDefaultDataSource() {
//        X delegate = getDelegate();
//        return delegate.newDefaultDataSource();
//    }
}

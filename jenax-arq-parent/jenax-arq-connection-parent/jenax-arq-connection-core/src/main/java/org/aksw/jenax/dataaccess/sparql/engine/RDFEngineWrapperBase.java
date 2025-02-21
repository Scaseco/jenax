package org.aksw.jenax.dataaccess.sparql.engine;

import org.aksw.commons.util.closeable.AutoCloseableWrapperBase;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.apache.jena.sparql.core.DatasetGraph;

public class RDFEngineWrapperBase<X extends RDFEngine>
    extends AutoCloseableWrapperBase<X>
    implements RDFEngine
{
    public RDFEngineWrapperBase(X delegate) {
        super(delegate);
    }

     @Override
    public RDFLinkBuilder newLinkBuilder() {
        X tmp = getDelegate();
        return tmp.newLinkBuilder();
    }

    @Override
    public DatasetGraph getDataset() {
        X tmp = getDelegate();
        return tmp.getDataset();
    }

/*
    @Override
    public RDFDataSource newDefaultDataSource() {
        X tmp = getDelegate();
        return tmp.newDefaultDataSource();
    }
*/
}

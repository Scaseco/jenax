package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngineWrapperBase;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderOverRDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.apache.jena.sparql.core.DatasetGraph;

public class WrappedRDFEngine<X extends RDFEngine>
    extends RDFEngineWrapperBase<X>
{
    protected RDFLinkSource effectiveLinkSource;

    public WrappedRDFEngine(X delegate, RDFLinkSource effectiveLinkSource) {
        super(delegate);
        this.effectiveLinkSource = Objects.requireNonNull(effectiveLinkSource);
    }

    @Override
    public RDFLinkBuilder newLinkBuilder() {
        return new RDFLinkBuilderOverRDFLinkSource(effectiveLinkSource);
    }

    @Override
    public void close() throws Exception {
        getDelegate().close();
    }

    @Override
    public DatasetGraph getDataset() {
        return getDelegate().getDataset();
    }
}

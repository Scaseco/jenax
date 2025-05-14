package org.aksw.jenax.dataaccess.sparql.linksource;

import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.apache.jena.sparql.core.DatasetGraph;

public interface RDFLinkSourceWrapper<X extends RDFLinkSource>
    extends RDFLinkSource
{
    X getDelegate();

    @Override
    default DatasetGraph getDatasetGraph() {
        return getDelegate().getDatasetGraph();
    }

    @Override
    default RDFLinkBuilder<?> newLinkBuilder() {
        X tmp = getDelegate();
        return tmp.newLinkBuilder();
    }
}

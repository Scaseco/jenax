package org.aksw.jenax.dataaccess.sparql.linksource;

import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderOverLinkSupplier;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;

public class RDFLinkSourceOverDataset
    implements RDFLinkSource
{
    protected DatasetGraph datasetGraph;

    public RDFLinkSourceOverDataset(DatasetGraph datasetGraph) {
        super();
        this.datasetGraph = datasetGraph;
    }

    @Override
    public DatasetGraph getDatasetGraph() {
        return datasetGraph;
    }

    @Override
    public RDFLinkBuilder<?> newLinkBuilder() {
        return new RDFLinkBuilderOverLinkSupplier<>(() -> RDFLink.connect(datasetGraph));
    }
}

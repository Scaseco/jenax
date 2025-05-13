package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderOverRDFLinkSource;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Link source over a DatasetGraph.
 * In order to alter the default link creation, it is recommended to
 * override the {@link #newLink()} method.
 */
public class RDFLinkSourceOverDatasetGraph
    implements RDFLinkSource
{
    protected DatasetGraph datasetGraph;

    public RDFLinkSourceOverDatasetGraph(DatasetGraph datasetGraph) {
        super();
        this.datasetGraph = Objects.requireNonNull(datasetGraph);
    }

    @Override
    public DatasetGraph getDatasetGraph() {
        return datasetGraph;
    }

    @Override
    public RDFLinkBuilder<?> newLinkBuilder() {
        return new RDFLinkBuilderOverRDFLinkSource<>(this);
    }

    @Override
    public RDFLink newLink() {
        return RDFLink.connect(datasetGraph);
    }

    @Override
    public String toString() {
        return "RDFLinkSourceOverDatasetGraph [datasetGraph=" + System.identityHashCode(datasetGraph) + "]";
    }
}

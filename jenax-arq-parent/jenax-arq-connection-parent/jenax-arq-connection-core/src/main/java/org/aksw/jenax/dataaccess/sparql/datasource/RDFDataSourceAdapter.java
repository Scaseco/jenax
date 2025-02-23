package org.aksw.jenax.dataaccess.sparql.datasource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.link.transform.LinkSparqlQueryTransformApp;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransforms;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceAdapter;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;

public class RDFDataSourceAdapter
    implements RDFDataSource
{
    protected RDFLinkSource linkSource;

    protected RDFDataSourceAdapter(RDFLinkSource linkSource) {
        super();
        this.linkSource = linkSource;
    }

    @Override
    public Dataset getDataset() {
        // Cache as field?
        DatasetGraph dsg = linkSource.getDatasetGraph();
        Dataset ds = dsg == null ? null : DatasetFactory.wrap(dsg);
        return ds;
    }

    public RDFLinkSource getLinkSource() {
        return linkSource;
    }

    @Override
    public RDFConnection getConnection() {
        RDFLink link = linkSource.newLink();
        DatasetGraph dsg = linkSource.getDatasetGraph();
        RDFConnection result = adapt(link, dsg);
        return result;
    }

    public static RDFConnection adapt(RDFLink link, DatasetGraph dsg) {
        Objects.requireNonNull(link);
        RDFLink finalLink = dsg == null
            ? link
            : RDFLinkTransforms.of(new LinkSparqlQueryTransformApp(dsg)).apply(link);
        RDFConnection result = RDFConnectionAdapter.adapt(finalLink);
        return result;
    }

    public static RDFDataSource adapt(RDFLinkSource linkSource) {
        Objects.requireNonNull(linkSource);
        RDFDataSource result = linkSource instanceof RDFLinkSourceAdapter adapter
            ? adapter.getDelegate()
            : new RDFDataSourceAdapter(linkSource);
        return result;
    }
}

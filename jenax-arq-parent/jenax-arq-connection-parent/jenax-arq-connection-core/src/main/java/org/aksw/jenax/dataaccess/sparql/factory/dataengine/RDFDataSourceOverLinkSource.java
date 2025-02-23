package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.link.transform.LinkSparqlQueryTransformApp;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransforms;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;

public class RDFDataSourceOverLinkSource
    implements RDFDataSource
{
    protected RDFLinkSource linkSource;

    public RDFDataSourceOverLinkSource(RDFLinkSource linkSource) {
        super();
        this.linkSource = linkSource;
    }

    @Override
    public RDFConnection getConnection() {
        RDFLink link = linkSource.newLink();

        DatasetGraph dsg = linkSource.getDatasetGraph();
        if (dsg != null) {
            link = RDFLinkTransforms.of(new LinkSparqlQueryTransformApp(dsg)).apply(link);
        }

        return RDFConnectionAdapter.adapt(link);
    }
}

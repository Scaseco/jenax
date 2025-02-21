package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;

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
        return RDFConnectionAdapter.adapt(link);
    }
}

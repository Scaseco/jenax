package org.aksw.jenax.dataaccess.sparql.datasource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceAdapter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;

public class RDFDataSourceAdapter
    implements RDFDataSource
{
    protected RDFLinkSource delegate;

    protected RDFDataSourceAdapter(RDFLinkSource linkSource) {
        super();
        this.delegate = Objects.requireNonNull(linkSource);
    }

    public RDFLinkSource getDelegate() {
        return delegate;
    }

    @Override
    public RDFConnection getConnection() {
        RDFLink link = delegate.newLink();
        return RDFConnectionAdapter.adapt(link);
    }

    public static RDFDataSource adapt(RDFLinkSource linkSource) {
        RDFDataSource result = linkSource instanceof RDFLinkSourceAdapter adapter
            ? adapter.getDelegate()
            : new RDFDataSourceAdapter(linkSource);
        return result;
    }
}

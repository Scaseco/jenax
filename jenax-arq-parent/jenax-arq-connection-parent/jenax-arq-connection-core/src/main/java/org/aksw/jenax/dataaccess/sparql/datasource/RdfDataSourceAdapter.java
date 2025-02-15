package org.aksw.jenax.dataaccess.sparql.datasource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.linksource.RdfLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RdfLinkSourceAdapter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;

public class RdfDataSourceAdapter
    implements RdfDataSource
{
    protected RdfLinkSource delegate;

    protected RdfDataSourceAdapter(RdfLinkSource linkSource) {
        super();
        this.delegate = Objects.requireNonNull(linkSource);
    }

    public RdfLinkSource getDelegate() {
        return delegate;
    }

    @Override
    public RDFConnection getConnection() {
        RDFLink link = delegate.newLink();
        return RDFConnectionAdapter.adapt(link);
    }

    public RdfDataSource adapt(RdfLinkSource linkSource) {
        RdfDataSource result = linkSource instanceof RdfLinkSourceAdapter adapter
            ? adapter.getDelegate()
            : new RdfDataSourceAdapter(linkSource);
        return result;
    }
}

package org.aksw.jenax.dataaccess.sparql.datasource;

import java.util.Objects;

import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkAdapter;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecBuilderAdapter;

public class RdfLinkSourceAdapter
    implements RdfLinkSource
{
    protected RdfDataSource delegate;

    public RdfLinkSourceAdapter(RdfDataSource delegate) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public RDFLink newLink() {
        RDFConnection conn = delegate.getConnection();
        RDFLink result = RDFLinkAdapter.adapt(conn);
        return result;
    }

    @Override
    public QueryExecBuilder newQuery() {
        QueryExecutionBuilder builder = delegate.newQuery();
        QueryExecBuilder result = QueryExecBuilderAdapter.adapt(builder);
        return result;
    }

    @Override
    public RdfDataSource asDataSource() {
        return delegate;
    }
}

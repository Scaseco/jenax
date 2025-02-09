package org.aksw.jenax.dataaccess.sparql.datasource;

import java.util.Objects;

import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkAdapter;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecBuilderAdapter;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecBuilderAdapter;
import org.apache.jena.update.UpdateExecutionBuilder;

public class RdfLinkSourceAdapter
    implements RdfLinkSource
{
    protected RdfDataSource delegate;

    public RdfLinkSourceAdapter(RdfDataSource delegate) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
    }

    public RdfDataSource getDelegate() {
        return delegate;
    }

    @Override
    public RDFLink newLink() {
        RDFConnection conn = getDelegate().getConnection();
        RDFLink result = RDFLinkAdapter.adapt(conn);
        return result;
    }

    @Override
    public QueryExecBuilder newQuery() {
        QueryExecutionBuilder builder = getDelegate().newQuery();
        QueryExecBuilder result = QueryExecBuilderAdapter.adapt(builder);
        return result;
    }

    @Override
    public UpdateExecBuilder newUpdate() {
        UpdateExecutionBuilder builder = getDelegate().newUpdate();
        UpdateExecBuilder result = UpdateExecBuilderAdapter.adapt(builder);
        return result;
    }

//    @Override
//    public RdfDataSource asDataSource() {
//        return delegate;
//    }
}

package org.aksw.jenax.dataaccess.sparql.connection.query;

import org.aksw.jenax.dataaccess.sparql.execution.query.QueryExecutionTransform;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class SparqlQueryConnectionWithExecTransform
    implements SparqlQueryConnectionTmp
{
    protected SparqlQueryConnection delegate;
    protected QueryExecutionTransform execTransform;

    public SparqlQueryConnectionWithExecTransform(SparqlQueryConnection delegate,
            QueryExecutionTransform execTransform) {
        super();
        this.delegate = delegate;
        this.execTransform = execTransform;
    }

    @Override
    public SparqlQueryConnection getDelegate() {
        return delegate;
    }

    @Override
    public QueryExecution query(Query query) {
        QueryExecution before = delegate.query(query);
        QueryExecution after = execTransform.apply(before);
        return after;
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public QueryExecutionBuilder newQuery() {
        return delegate.newQuery();
    }
}

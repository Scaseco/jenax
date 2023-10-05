package org.aksw.jenax.dataaccess.sparql.execution.factory.query;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class QueryExecutionFactoryOverSparqlQueryConnection
    implements QueryExecutionFactory
{
    protected SparqlQueryConnection conn;
    protected boolean closeDelegateOnClose;

    public QueryExecutionFactoryOverSparqlQueryConnection(SparqlQueryConnection conn) {
        this(conn, true);
    }

    public QueryExecutionFactoryOverSparqlQueryConnection(SparqlQueryConnection conn, boolean closeDelegate) {
        super();
        this.conn = conn;
        this.closeDelegateOnClose = closeDelegate;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        return conn.query(queryString);
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        return conn.query(query);
    }

    @Override
    public void close() throws Exception {
        if(closeDelegateOnClose) {
            conn.close();
        }
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getState() {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T result = getClass().isAssignableFrom(clazz) ? (T)this : null;
        return result;
    }
}

package org.aksw.jenax.dataaccess.sparql.exec.query;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecAdapter;

public class QueryExecFactoryOverQueryExecutionFactory
    implements QueryExecFactory
{
    protected QueryExecutionFactory qef;
    protected boolean closeDelegateOnClose;

    public QueryExecFactoryOverQueryExecutionFactory(QueryExecutionFactory qef) {
        this(qef, true);
    }

    public QueryExecFactoryOverQueryExecutionFactory(QueryExecutionFactory qef, boolean closeDelegate) {
        super();
        this.qef = qef;
        this.closeDelegateOnClose = closeDelegate;
    }

    public QueryExecutionFactory getDecoratee() {
        return qef;
    }

    @Override
    public QueryExec create(String queryString) {
        QueryExecution qe = qef.createQueryExecution(queryString);
        return QueryExecAdapter.adapt(qe);
    }

    @Override
    public QueryExec create(Query query) {
        QueryExecution qe = qef.createQueryExecution(query);
        return QueryExecAdapter.adapt(qe);
    }

    @Override
    public void close() throws Exception {
        qef.close();
    }

//	public void close() throws Exception {
//	    if(closeDelegateOnClose) {
//	        // conn.close();
//	    }
//	}
//
//	@Override
//	public String getId() {
//	    return null;
//	}
//
//	@Override
//	public String getState() {
//	    return null;
//	}
//
//	@Override
//	public <T> T unwrap(Class<T> clazz) {
//	    @SuppressWarnings("unchecked")
//	    T result = getClass().isAssignableFrom(clazz) ? (T)this : null;
//	    return result;
//	}
}

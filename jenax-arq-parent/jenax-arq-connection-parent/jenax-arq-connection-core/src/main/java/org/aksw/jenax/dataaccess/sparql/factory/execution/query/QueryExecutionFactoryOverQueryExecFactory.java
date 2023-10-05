package org.aksw.jenax.dataaccess.sparql.factory.execution.query;

import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecutionAdapter;

public class QueryExecutionFactoryOverQueryExecFactory
	implements QueryExecutionFactory
{
	protected QueryExecFactory queryExecFactory;
	protected boolean closeDelegateOnClose;

	public QueryExecutionFactoryOverQueryExecFactory(QueryExecFactory queryExecFactory) {
	    this(queryExecFactory, true);
	}

	public QueryExecutionFactoryOverQueryExecFactory(QueryExecFactory queryExecFactory, boolean closeDelegate) {
	    super();
	    this.queryExecFactory = queryExecFactory;
	    this.closeDelegateOnClose = closeDelegate;
	}

	public QueryExecFactory getDecoratee() {
		return queryExecFactory;
	}

	@Override
	public QueryExecution createQueryExecution(String queryString) {
	    QueryExec qe = queryExecFactory.create(queryString);
	    return QueryExecutionAdapter.adapt(qe);
	}

	@Override
	public QueryExecution createQueryExecution(Query query) {
	    QueryExec qe = queryExecFactory.create(query);
	    return QueryExecutionAdapter.adapt(qe);
	}

	@Override
	public void close() throws Exception {
	    if(closeDelegateOnClose) {
	        // conn.close();
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

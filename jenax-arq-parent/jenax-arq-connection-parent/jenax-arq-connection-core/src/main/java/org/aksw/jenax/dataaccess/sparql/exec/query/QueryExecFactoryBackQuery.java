package org.aksw.jenax.dataaccess.sparql.exec.query;

import java.util.function.Function;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.exec.QueryExec;

public class QueryExecFactoryBackQuery
	implements QueryExecFactory
{
	protected QueryExecFactoryQuery delegate;
	protected Function<String, Query> queryParser;

	public QueryExecFactoryBackQuery(QueryExecFactoryQuery decoratee, Function<String, Query> queryParser) {
		super();
		this.delegate = decoratee;
		this.queryParser = queryParser;
	}

	@Override
	public QueryExec create(Query query) {
		return delegate.create(query);
	}

	@Override
	public QueryExec create(String queryString) {
		Query query = queryParser.apply(queryString);
		QueryExec result = create(query);
		return result;
	}

	@Override
	public void close() throws Exception {
		if (delegate instanceof AutoCloseable) {
			((AutoCloseable)delegate).close();
		}
	}
}

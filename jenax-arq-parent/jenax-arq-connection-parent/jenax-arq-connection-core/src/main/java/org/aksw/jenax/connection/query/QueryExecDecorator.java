package org.aksw.jenax.connection.query;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;

public interface QueryExecDecorator
	extends QueryExec
{
    QueryExec getDecoratee();

	default Context getContext() {
		return getDecoratee().getContext();
	}

	@Override
	default Query getQuery() {
		return getDecoratee().getQuery();
	}

	@Override
	default String getQueryString() {
		return getDecoratee().getQueryString();
	}

	@Override
	default void close() {
		getDecoratee().close();
	}

	@Override
	default boolean isClosed() {
		return getDecoratee().isClosed();
	}
    
	@Override
	default void abort() {
		getDecoratee().abort();
	}
}

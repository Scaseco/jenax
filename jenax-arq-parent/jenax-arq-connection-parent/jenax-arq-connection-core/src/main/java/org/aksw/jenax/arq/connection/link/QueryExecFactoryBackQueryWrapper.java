package org.aksw.jenax.arq.connection.link;

import java.util.function.Function;

import org.apache.jena.query.Query;

public class QueryExecFactoryBackQueryWrapper
	extends QueryExecFactoryBackQuery
{
	public QueryExecFactoryBackQueryWrapper(QueryExecFactoryQuery decoratee, Function<String, Query> queryParser) {
		super(decoratee, queryParser);
	}

	public QueryExecFactoryQuery getDecoratee() {
		return decoratee;
	}
}

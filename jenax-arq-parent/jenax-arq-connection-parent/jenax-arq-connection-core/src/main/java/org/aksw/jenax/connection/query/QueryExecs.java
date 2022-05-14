package org.aksw.jenax.connection.query;

import org.aksw.jenax.arq.util.exception.HttpExceptionUtils;
import org.apache.jena.sparql.exec.QueryExec;

public class QueryExecs {
	public static QueryExec withDetailedHttpMessages(QueryExec qe) {
		return new QueryExecDecoratorBase<QueryExec>(qe) {
			@Override
			public void onException(Exception e) {
				RuntimeException f = HttpExceptionUtils.makeHumanFriendly(e);
				throw f;
			}
		};
	}
}

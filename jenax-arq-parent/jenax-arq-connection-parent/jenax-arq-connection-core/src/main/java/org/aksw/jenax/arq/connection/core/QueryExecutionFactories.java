package org.aksw.jenax.arq.connection.core;

import org.aksw.jenax.arq.connection.link.QueryExecFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class QueryExecutionFactories {
	public static QueryExecutionFactory adapt(QueryExecFactory queryExecFactory) {
		return new QueryExecutionFactoryOverQueryExecFactory(queryExecFactory);
	}

	public static QueryExecutionFactory of(SparqlQueryConnection conn) {
		return new QueryExecutionFactoryOverSparqlQueryConnection(conn);
	}

}

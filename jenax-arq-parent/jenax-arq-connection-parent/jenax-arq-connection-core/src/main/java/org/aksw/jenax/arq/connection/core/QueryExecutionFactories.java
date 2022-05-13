package org.aksw.jenax.arq.connection.core;

import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class QueryExecutionFactories {
	public static QueryExecutionFactory adapt(SparqlQueryConnection conn) {
		return new QueryExecutionFactoryOverSparqlQueryConnection(conn);
	}
}

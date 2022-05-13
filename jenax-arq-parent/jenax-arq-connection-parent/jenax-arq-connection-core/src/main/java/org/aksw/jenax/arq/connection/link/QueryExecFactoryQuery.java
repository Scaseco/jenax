package org.aksw.jenax.arq.connection.link;

import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecAdapter;

@FunctionalInterface
public interface QueryExecFactoryQuery
	// extends Function<Query, QueryExec>
{
	QueryExec create(Query query);

    /** Return a factory for the QueryExecution level */
    default QueryExecutionFactoryQuery levelUp() {
    	return QueryExecutionFactoryQuery.adapt(this);
    }


	public static QueryExecFactoryQuery adapt(QueryExecutionFactoryQuery qef) {
		return query -> QueryExecAdapter.adapt(qef.createQueryExecution(query));
	}
}

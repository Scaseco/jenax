package org.aksw.jenax.connection.query;


import org.aksw.jenax.arq.connection.link.QueryExecFactoryQuery;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.exec.QueryExecutionAdapter;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:30 PM
 */
@FunctionalInterface
public interface QueryExecutionFactoryQuery {
    QueryExecution createQueryExecution(Query query);

    /** Return a factory for the QueryExec level */
    default QueryExecFactoryQuery levelDown() {
    	return QueryExecFactoryQuery.adapt(this);
    }

	public static QueryExecutionFactoryQuery adapt(QueryExecFactoryQuery qef) {
		return query -> QueryExecutionAdapter.adapt(qef.create(query));
	}
}

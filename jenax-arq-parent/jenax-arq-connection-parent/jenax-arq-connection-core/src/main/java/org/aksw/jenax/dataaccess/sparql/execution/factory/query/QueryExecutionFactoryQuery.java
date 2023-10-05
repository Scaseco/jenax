package org.aksw.jenax.dataaccess.sparql.execution.factory.query;


import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:30 PM
 */
@FunctionalInterface
public interface QueryExecutionFactoryQuery {

    QueryExecution createQueryExecution(Query query);

    /* Convenience shorthands */

    default Model execConstruct(Query query) {
        try (QueryExecution qe = createQueryExecution(query)) {
            return qe.execConstruct();
        }
    }
    default Dataset execConstructDataset(Query query) {
        try (QueryExecution qe = createQueryExecution(query)) {
            return qe.execConstructDataset();
        }
    }

    default Model execDescribe(Query query) {
        try (QueryExecution qe = createQueryExecution(query)) {
            return qe.execDescribe();
        }
    }

    /** Convenience method to execute a construct query and add the result to the model. */
    default void execConstruct(Model model, Query query) {
        try (QueryExecution qe = createQueryExecution(query)) {
            qe.execConstruct(model);
        }
    }

    /** Convenience method to execute a construct quads query and add the result to the dataset. */
    default void execConstructDataset(Dataset dataset, Query query) {
        try (QueryExecution qe = createQueryExecution(query)) {
            qe.execConstructDataset(dataset);
        }
    }

    /** Convenience method to execute a describe query and add the result to the model. */
    default void execDescribe(Model model, Query query) {
        try (QueryExecution qe = createQueryExecution(query)) {
            qe.execDescribe(model);
        }
    }


    /** Return a factory for the QueryExec level */
//    default QueryExecFactoryQuery levelDown() {
//    	return QueryExecFactoryQuery.adapt(this);
//    }
//
//	public static QueryExecutionFactoryQuery adapt(QueryExecFactoryQuery qef) {
//		return query -> QueryExecutionAdapter.adapt(qef.create(query));
//	}
}

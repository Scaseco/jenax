package org.aksw.jenax.arq.connection.core;

import org.apache.jena.query.QueryExecution;


/**
 * @author Claus Stadler
 *
 *
 *         Date: 7/23/11
 *         Time: 9:25 PM
 */
public interface QueryExecutionFactoryString {
    QueryExecution createQueryExecution(String queryString);
}

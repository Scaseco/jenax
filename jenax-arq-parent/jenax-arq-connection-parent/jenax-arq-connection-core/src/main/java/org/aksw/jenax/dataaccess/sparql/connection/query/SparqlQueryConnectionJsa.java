package org.aksw.jenax.dataaccess.sparql.connection.query;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.TransactionalNull;


public class SparqlQueryConnectionJsa
    extends SparqlQueryConnectionJsaBase<QueryExecutionFactory>
{
    public SparqlQueryConnectionJsa(QueryExecutionFactory queryExecutionFactory) {
        super(queryExecutionFactory, new TransactionalNull());
    }

    @Override
    public QueryExecution query(String queryStr) {
        QueryExecution result = queryExecutionFactory.createQueryExecution(queryStr);
        return result;
    }

    @Override
    public void close() {
        try {
            queryExecutionFactory.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}

package org.aksw.jenax.arq.connection.core;

import org.aksw.jenax.arq.connection.SparqlQueryConnectionJsaBase;
import org.aksw.jenax.arq.connection.TransactionalTmp;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.Transactional;


public class SparqlQueryConnectionJsa
    extends SparqlQueryConnectionJsaBase<QueryExecutionFactory>
{
    public SparqlQueryConnectionJsa(QueryExecutionFactory queryExecutionFactory) {
        super(queryExecutionFactory, new TransactionalTmp() {
            @Override
            public Transactional getDelegate() {
                return null;
            }});
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

package org.aksw.jenax.arq.connection;

import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.sparql.core.Transactional;

public class SparqlQueryConnectionJsaBase<T extends QueryExecutionFactoryQuery>
    extends TransactionalDelegate
    implements SparqlQueryConnectionTmp
{
    protected T queryExecutionFactory;
    protected Transactional transactional;

    public SparqlQueryConnectionJsaBase(T queryExecutionFactory) {
        this(queryExecutionFactory, new TransactionalTmp() {
            @Override
            public Transactional getDelegate() {
                return null;
            }});
    }

    public SparqlQueryConnectionJsaBase(T queryExecutionFactory, Transactional transactional) {
        super();
        this.queryExecutionFactory = queryExecutionFactory;
        this.transactional = transactional;
    }

    @Override
    public Transactional getDelegate() {
        return transactional;
    }

    @Override
    public QueryExecution query(Query query) {
        QueryExecution result = queryExecutionFactory.createQueryExecution(query);
        return result;
    }

    @Override
    public void close() {
    }

    @Override
    public QueryExecutionBuilder newQuery() {
        // TODO
        throw new UnsupportedOperationException();
    }
}
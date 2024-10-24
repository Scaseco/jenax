package org.aksw.jenax.dataaccess.sparql.connection.query;

import org.aksw.jenax.dataaccess.sparql.common.TransactionalWrapper;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNull;

@Deprecated /** Use RdfDataEngineOverQueryExecutionFactory */
public class SparqlQueryConnectionJsaBase<T extends QueryExecutionFactoryQuery>
    implements TransactionalWrapper, SparqlQueryConnectionTmp
{
    protected T queryExecutionFactory;
    protected Transactional transactional;

    public SparqlQueryConnectionJsaBase(T queryExecutionFactory) {
        this(queryExecutionFactory, new TransactionalNull());
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

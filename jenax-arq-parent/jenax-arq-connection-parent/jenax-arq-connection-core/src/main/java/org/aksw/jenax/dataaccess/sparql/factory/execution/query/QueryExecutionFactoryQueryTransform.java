package org.aksw.jenax.dataaccess.sparql.factory.execution.query;

import org.aksw.jenax.arq.util.query.QueryTransform;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class QueryExecutionFactoryQueryTransform
    extends QueryExecutionFactoryDecorator
{
    protected QueryTransform transform;

    public QueryExecutionFactoryQueryTransform(QueryExecutionFactory decoratee, QueryTransform transform) {
        super(decoratee);
        this.transform = transform;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        throw new RuntimeException("Query must be parsed");
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        Query tmp = transform.apply(query);
        QueryExecution result = super.createQueryExecution(tmp);
        return result;
    }
}

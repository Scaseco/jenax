package org.aksw.jena_sparql_api.parse;

import java.util.function.Function;

import org.aksw.jenax.dataaccess.sparql.execution.factory.query.QueryExecutionFactory;
import org.aksw.jenax.dataaccess.sparql.execution.factory.query.QueryExecutionFactoryDecorator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class QueryExecutionFactoryParse
    extends QueryExecutionFactoryDecorator
{
    protected Function<String, Query> parser;

    public QueryExecutionFactoryParse(QueryExecutionFactory decoratee, Function<String, Query> parser) {
        super(decoratee);
        this.parser = parser;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        Query query = parser.apply(queryString);
        QueryExecution result = createQueryExecution(query);
        return result;
    }

}

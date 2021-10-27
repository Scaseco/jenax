package org.aksw.jena_sparql_api.limit;

import org.aksw.jena_sparql_api.transform.QueryExecutionFactoryDecorator;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;

/**
 * A query execution that sets a limit on all queries
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/19/11
 *         Time: 11:33 PM
 */
public class QueryExecutionFactoryLimit
    extends QueryExecutionFactoryDecorator
{
    private Long limit;
    private boolean doCloneQuery = false;

    public static <U extends QueryExecution> QueryExecutionFactoryLimit decorate(QueryExecutionFactory decoratee, boolean doCloneQuery, Long limit) {
        return new QueryExecutionFactoryLimit(decoratee, doCloneQuery, limit);
    }

    public QueryExecutionFactoryLimit(QueryExecutionFactory decoratee, boolean doCloneQuery, Long limit) {
        super(decoratee);
        this.limit = limit;
    }

    public QueryExecution createQueryExecution(Query query) {
        if(limit != null) {
            if(query.getLimit() == Query.NOLIMIT) {
                if(doCloneQuery) {
                    query = query.cloneQuery();
                }

                query.setLimit(limit);
            } else {
                long adjustedLimit = Math.min(limit, query.getLimit());

                if(adjustedLimit != query.getLimit()) {
                    if(doCloneQuery) {
                        query = query.cloneQuery();
                    }

                    query.setLimit(adjustedLimit);
                }
            }
        }

        return super.createQueryExecution(query);
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        Query query = QueryFactory.create(queryString);
        QueryExecution result = createQueryExecution(query);
        return result;
    }

}

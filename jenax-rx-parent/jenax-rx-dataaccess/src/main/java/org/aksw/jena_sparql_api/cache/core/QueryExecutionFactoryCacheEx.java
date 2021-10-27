package org.aksw.jena_sparql_api.cache.core;

import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.transform.QueryExecutionFactoryDecorator;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 4:08 PM
 */
public class QueryExecutionFactoryCacheEx
        extends QueryExecutionFactoryDecorator
{
    private CacheFrontend cache;
    private String service;

    public QueryExecutionFactoryCacheEx(QueryExecutionFactory decoratee, CacheFrontend cache) {
        this(decoratee, decoratee.getId() + "#" + decoratee.getState(), cache);
    }

    public QueryExecutionFactoryCacheEx(QueryExecutionFactory decoratee, String service, CacheFrontend cache) {
        super(decoratee);
        this.service = service;
        this.cache = cache;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        return new QueryExecutionCacheEx(super.createQueryExecution(query), service, query.toString(), cache);
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        return new QueryExecutionCacheEx(super.createQueryExecution(queryString), service, queryString, cache);
    }
}

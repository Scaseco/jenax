package org.aksw.jena_sparql_api.transform;

import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 12:53 PM
 */
public class QueryExecutionFactoryDecorator
    extends QueryExecutionFactoryDecoratorBase<QueryExecutionFactory>
{
    public QueryExecutionFactoryDecorator(QueryExecutionFactory decoratee) {
        super(decoratee);
    }
}


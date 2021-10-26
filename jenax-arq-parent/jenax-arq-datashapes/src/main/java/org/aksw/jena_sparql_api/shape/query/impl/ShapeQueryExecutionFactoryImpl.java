package org.aksw.jena_sparql_api.shape.query.impl;

import org.aksw.jena_sparql_api.shape.query.api.ShapeQueryExecution;
import org.aksw.jena_sparql_api.shape.query.api.ShapeQueryExecutionFactory;
import org.aksw.jena_sparql_api.shape.syntax.ShapeQuery;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;

public class ShapeQueryExecutionFactoryImpl
    implements ShapeQueryExecutionFactory
{
    protected QueryExecutionFactory qef;


    @Override
    public ShapeQueryExecution createShapeExecution(ShapeQuery query) {
        ShapeQueryExecution result = new ShapeQueryExecutionImpl();

        return result;
    }

}

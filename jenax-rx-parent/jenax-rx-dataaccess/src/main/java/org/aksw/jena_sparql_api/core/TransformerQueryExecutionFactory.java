package org.aksw.jena_sparql_api.core;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;

import com.google.common.base.Function;

public interface TransformerQueryExecutionFactory
    extends Function<QueryExecutionFactory, QueryExecutionFactory>
{
}

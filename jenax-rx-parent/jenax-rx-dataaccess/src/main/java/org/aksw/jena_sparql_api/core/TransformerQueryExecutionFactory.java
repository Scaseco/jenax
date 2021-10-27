package org.aksw.jena_sparql_api.core;

import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;

import com.google.common.base.Function;

public interface TransformerQueryExecutionFactory
    extends Function<QueryExecutionFactory, QueryExecutionFactory>
{
}

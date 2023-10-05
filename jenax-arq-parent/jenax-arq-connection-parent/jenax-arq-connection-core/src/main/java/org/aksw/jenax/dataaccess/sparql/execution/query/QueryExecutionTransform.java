package org.aksw.jenax.dataaccess.sparql.execution.query;

import java.util.function.Function;

import org.apache.jena.query.QueryExecution;

public interface QueryExecutionTransform
    extends Function<QueryExecution, QueryExecution>
{
}

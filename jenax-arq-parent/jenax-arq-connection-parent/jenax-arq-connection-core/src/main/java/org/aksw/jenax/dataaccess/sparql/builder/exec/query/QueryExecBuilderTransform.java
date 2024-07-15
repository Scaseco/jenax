package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

import java.util.function.Function;

import org.apache.jena.sparql.exec.QueryExecBuilder;

public interface QueryExecBuilderTransform
    extends Function<QueryExecBuilder, QueryExecBuilder>
{
}

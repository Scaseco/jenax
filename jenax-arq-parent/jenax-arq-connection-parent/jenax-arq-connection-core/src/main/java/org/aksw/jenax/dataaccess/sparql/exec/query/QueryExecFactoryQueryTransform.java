package org.aksw.jenax.dataaccess.sparql.exec.query;

import java.util.function.Function;

/** Transforms a {@link QueryExecFactoryQuery} into another one, typically by decorating it. */
@FunctionalInterface
public interface QueryExecFactoryQueryTransform
    extends Function<QueryExecFactoryQuery, QueryExecFactoryQuery>
{
}

package org.aksw.jenax.arq.connection.link;

import java.util.function.Function;

@FunctionalInterface
public interface QueryExecFactoryQueryMod
	extends Function<QueryExecFactoryQuery, QueryExecFactoryQuery>
{
}

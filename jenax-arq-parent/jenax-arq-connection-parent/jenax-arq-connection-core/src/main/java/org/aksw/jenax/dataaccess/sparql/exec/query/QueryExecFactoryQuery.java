package org.aksw.jenax.dataaccess.sparql.exec.query;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.exec.QueryExec;

@FunctionalInterface
public interface QueryExecFactoryQuery
	// extends Function<Query, QueryExec>
{
	QueryExec create(Query query);
}

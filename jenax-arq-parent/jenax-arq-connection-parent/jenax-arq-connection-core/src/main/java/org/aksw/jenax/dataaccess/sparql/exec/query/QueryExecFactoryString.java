package org.aksw.jenax.dataaccess.sparql.exec.query;

import org.apache.jena.sparql.exec.QueryExec;

@FunctionalInterface
public interface QueryExecFactoryString
{
	QueryExec create(String queryString);
}

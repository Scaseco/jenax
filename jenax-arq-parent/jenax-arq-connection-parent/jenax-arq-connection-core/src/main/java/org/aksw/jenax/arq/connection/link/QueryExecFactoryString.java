package org.aksw.jenax.arq.connection.link;

import org.apache.jena.sparql.exec.QueryExec;

@FunctionalInterface
public interface QueryExecFactoryString
{
	QueryExec create(String queryString);
}

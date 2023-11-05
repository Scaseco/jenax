package org.aksw.jenax.arq.util.exec.query;

import java.util.function.Function;

import org.apache.jena.sparql.exec.QueryExec;

@FunctionalInterface
public interface QueryExecTransform
    extends Function<QueryExec, QueryExec>
{
}

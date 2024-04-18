package org.aksw.jenax.stmt.core;

import java.util.function.Function;

@FunctionalInterface
public interface SparqlStmtTransform
    extends Function<SparqlStmt, SparqlStmt>
{
}

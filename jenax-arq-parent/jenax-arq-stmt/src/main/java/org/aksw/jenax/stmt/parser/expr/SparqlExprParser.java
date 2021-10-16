package org.aksw.jenax.stmt.parser.expr;

import java.util.function.Function;

import org.apache.jena.sparql.expr.Expr;

public interface SparqlExprParser
    extends Function<String, Expr>
{
}

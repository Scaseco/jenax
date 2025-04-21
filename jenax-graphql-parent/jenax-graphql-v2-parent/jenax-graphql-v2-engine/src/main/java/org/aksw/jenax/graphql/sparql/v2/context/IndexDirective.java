package org.aksw.jenax.graphql.sparql.v2.context;

import java.util.List;

/**
 * &#064;index(by: varOrVarArray[, expr: exprOverVars])
 * The idea is to allow ordering by a given list of variables, and have a separate expression for the effective output.
 * This may help a DBMS by not having to order by the final expression.
 */
// Perhaps extend indexVars to exprs
public record IndexDirective(String keyExpr, List<String> indexExprs, String oneIf) { }

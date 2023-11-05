package org.aksw.jenax.sparql.fragment.impl;

import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

public interface ExprFragment {
	Expr getExpr();
	Set<Var> getVarsMentioned();
}

package org.aksw.jenax.sparql.fragment.impl;

import java.util.Set;

import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

/**
 * FIXME This class is indended to be analoguous to the {@link Fragment} hierachy.
 * I.e. the intent is having annotated syntactic building blocks for
 * composing sparql queries 
 * 
 * This class should not be used with graph pattern expressions, i.e. (NOT) EXISTS
 *  
 * @author Claus Stadler, Sep 6, 2018
 *
 */
public interface XExpr {
	Set<Var> getVarsMentioned();
	Expr getExpr();
}

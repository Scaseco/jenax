package org.aksw.jenax.arq.schema_mapping;

import org.apache.jena.sparql.expr.Expr;

public interface ExprRewrite {
	Expr rewrite(Expr arg);
}

package org.aksw.jenax.graphql.sparql.v2.context;

import java.util.Optional;

import org.aksw.jenax.graphql.util.GraphQlUtils;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

import graphql.language.Directive;

public record BindDirective(String exprStr, String varName, Boolean isTarget)
    implements PrefixExpandable<BindDirective>
{
    public static final DirectiveParser<BindDirective> PARSER = new DirectiveParserImpl<>(BindDirective.class, "bind", false, BindDirective::parse);

    public static BindDirective parse(Directive directive) {
        String exprStr = GraphQlUtils.getArgAsString(directive, "of");
        String as = GraphQlUtils.getArgAsString(directive, "as");
        boolean isTarget = Optional.ofNullable(GraphQlUtils.getArgAsBoolean(directive, "target")).orElse(false);
        BindDirective result = new BindDirective(exprStr, as, isTarget);
        return result;
    }

    public Var getVar() {
        return varName == null ? null : Var.alloc(varName);
    }

    public Expr parseExpr() {
        Expr result = ExprUtils.parse(exprStr);
        return result;
    }

    @Override
    public BindDirective expand(PrefixMapping pm) {
        Expr expr = ExprUtils.parse(exprStr, pm);
        String newExprStr = ExprUtils.fmtSPARQL(expr);
        return new BindDirective(newExprStr, varName, isTarget);
    }

    public Directive toDirective() {
        return GraphQlUtils.newDirective("bind",
            GraphQlUtils.newArgString("of", exprStr),
            GraphQlUtils.newArgString("as", varName),
            GraphQlUtils.newArgBoolean("target", isTarget));
    }
}

package org.aksw.jenax.arq.util.expr;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprFunction0;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprFunction3;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformCopy;

/** */
public class ExprTransformCollectFunctions
    extends ExprTransformCopy
{
    /** Accumulator for mentioned function IRIs. */
    protected Collection<String> functionIris;

    public ExprTransformCollectFunctions() {
        this(new LinkedHashSet<>());
    }

    public ExprTransformCollectFunctions(Collection<String> functionIrisAcc) {
        super();
        this.functionIris = Objects.requireNonNull(functionIrisAcc);
    }

    public static Set<String> mentionedFunctionIris(Op op) {
        Set<String> result = new LinkedHashSet<>();
        mentionedFunctionIris(result, op);
        return result;
    }

    public static void mentionedFunctionIris(Collection<String> acc, Op op) {
        ExprTransform xform = new ExprTransformCollectFunctions(acc);
        Transformer.transform(null, xform, op);
    }

    public Collection<String> getFunctionIris() {
        return functionIris;
    }

    protected void process(ExprFunction func) {
        String id;
        if ((id = func.getFunctionIRI()) != null) {
            functionIris.add(id);
        }
        // XXX Perhaps also collect names
    }

    @Override
    public Expr transform(ExprFunction0 func) {
        process(func);
        return super.transform(func);
    }

    @Override
    public Expr transform(ExprFunction1 func, Expr expr1) {
        process(func);
        return super.transform(func, expr1);
    }

    @Override
    public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2) {
        process(func);
        return super.transform(func, expr1, expr2);
    }

    @Override
    public Expr transform(ExprFunction3 func, Expr expr1, Expr expr2, Expr expr3) {
        process(func);
        return super.transform(func, expr1, expr2, expr3);
    }

    @Override
    public Expr transform(ExprFunctionN func, ExprList args) {
        process(func);
        return super.transform(func, args);
    }

    @Override
    public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg) {
        process(funcOp);
        return super.transform(funcOp, args, opArg);
    }
}

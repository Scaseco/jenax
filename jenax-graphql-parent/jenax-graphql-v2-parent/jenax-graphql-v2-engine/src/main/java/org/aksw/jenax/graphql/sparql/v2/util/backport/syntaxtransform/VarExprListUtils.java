package org.aksw.jenax.graphql.sparql.v2.util.backport.syntaxtransform;

import java.util.List;

import org.aksw.jenax.graphql.sparql.v2.util.ExprUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.graph.NodeTransform;

public class VarExprListUtils {
    private static Expr transform(Expr expr, ExprTransform exprTransform)
    {
        if ( expr == null || exprTransform == null )
            return expr ;
        return ExprTransformer.transform(exprTransform, expr) ;
    }

    /** Copied from package org.apache.jena.sparql.algebra.ApplyTransformVisitor */
    public static VarExprList transform(VarExprList varExpr, ExprTransform exprTransform)
    {
        List<Var> vars = varExpr.getVars() ;
        VarExprList varExpr2 = new VarExprList() ;
        boolean changed = false ;
        for ( Var v : vars )
        {
            Expr newVE = exprTransform.transform(new ExprVar(v));
            Var newV = newVE == null ? v : ((ExprVar)newVE).asVar();

            // Once changed is true, it stays true
            changed = changed || !v.equals(newV);

            Expr e = varExpr.getExpr(v) ;
            Expr e2 =  e ;
            if ( e != null )
                e2 = transform(e, exprTransform) ;
            if ( e2 == null )
                varExpr2.add(newV) ;
            else
                varExpr2.add(newV, e2) ;
            if ( e != e2 )
                changed = true ;
        }
        if ( ! changed )
            return varExpr ;
        return varExpr2 ;
    }



    public static VarExprList transform(VarExprList vel, NodeTransform nodeTransform) {
        VarExprList result = new VarExprList();
        for (Var v : vel.getVars()) {
            Expr e = vel.getExpr(v);
            Var newV = (Var)nodeTransform.apply(v);
            Expr newExpr = e == null ? null : ExprUtils.applyNodeTransform(e, nodeTransform);
            result.add(newV, newExpr);
        }
        return result;
    }

    public static void replace(VarExprList dst, VarExprList src) {
        if(dst != src) {
            dst.clear();
            copy(dst, src);
        }
    }

    public static VarExprList copy(VarExprList dst, VarExprList src) {
        for(Var v : src.getVars()) {
            Expr e = src.getExpr(v);
            if(e == null) {
                dst.add(v);
            } else {
                dst.add(v, e);
            }
        }

        return dst;
    }

}

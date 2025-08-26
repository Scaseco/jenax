package org.aksw.jenax.facete.treequery2.impl;

import java.util.Set;

import org.aksw.facete.v3.api.FacetConstraintControl;
import org.aksw.jenax.arq.util.expr.NodeValueUtils;
import org.aksw.jenax.arq.util.node.NodeCustom;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * A specific expression
 */
public class ConstraintApi2Impl<T>
//implements FacetConstraintCore
{
    protected FacetConstraints<T> model;
    protected transient Expr exprVar;

    public ConstraintApi2Impl(FacetConstraints<T> model, T node) {
        super();
        this.model = model;
        // this.node = node;
        this.exprVar = ExprLib.nodeToExpr(NodeCustom.of(node));
    }

    public FacetConstraintControl createConstraint(Expr expr) {
        // TODO Extract the custom values
       Set<T> references = NodeCustom.mentionedValues(model.getConstraintClass(), expr);
       // model.model.row(references).computeIfAbsent(expr, e -> new ConstraintControl(model, references, expr));
       FacetConstraintControl result = new ConstraintControl2Impl<>(model, references, expr);
       return result;
    }

    public FacetConstraintControl exists() {
       Expr expr = new E_Bound(exprVar);
       FacetConstraintControl result = createConstraint(expr);
       return result;
    }

    /**
    * At present we allow a null argument to denote absent values.
    *
    */
    public FacetConstraintControl eq(Node node) {
       FacetConstraintControl result;
       if(node == null || NodeUtils.nullUriNode.equals(node)) {
           result = absent();
       } else {
           Expr expr = new E_Equals(exprVar, NodeValue.makeNode(node));
           result = createConstraint(expr);
       }

       return result;
    }


    public FacetConstraintControl absent() {
     Expr expr = new E_Equals(exprVar, NodeValueUtils.NV_ABSENT);
     FacetConstraintControl result = createConstraint(expr);
     return result;
    }

    public FacetConstraintControl regex(String pattern, String flags) {
     Expr expr = new E_Regex(new E_Str(exprVar), pattern, flags);
     FacetConstraintControl result = createConstraint(expr);
     return result;
    }
}

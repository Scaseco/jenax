package org.aksw.facete.v3.api;

import java.util.Set;

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
 * A helper for creating common constraints over a certain node in a FacetConstraints model
 *
 * TODO Move to impl package
 */
public class ConstraintApiImpl
//     implements FacetConstraintCore
{
    protected FacetConstraints model;
    protected TreeQueryNode node;

    protected transient Expr exprVar;

    public ConstraintApiImpl(FacetConstraints model, TreeQueryNode node) {
        super();
        this.model = model;
        this.node = node;
        this.exprVar = ExprLib.nodeToExpr(NodeCustom.of(node));
    }

    public FacetConstraintControl createConstraint(Expr expr) {
        Set<TreeQueryNode> references = NodeCustom.mentionedValues(TreeQueryNode.class, expr);
        // model.model.row(references).computeIfAbsent(expr, e -> new ConstraintControl(model, references, expr));
        FacetConstraintControl result = new FacetConstraintControlImpl(model, references, expr);
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


    /**
     * Create a range from a range from Java objects (such as Integers) via Jena's Type mapper
     *
     * @param range
     * @return
     */
/*
    public static Range<ComparableNodeValue> toNodeRange(Range<?> range) {
        TypeMapper tm = TypeMapper.getInstance();

        Node lowerNode = null;
        Node upperNode = null;

        BoundType lowerBoundType = null;
        BoundType upperBoundType = null;

        if(range.hasLowerBound()) {
            lowerBoundType = range.lowerBoundType();

            Object lb = range.lowerEndpoint();
            Class<?> lbClass = lb.getClass();

            RDFDatatype dtype = tm.getTypeByClass(lbClass);
            if(dtype == null) {
                throw new IllegalArgumentException("No type mapper entry for " + lbClass);
            }

            lowerNode = NodeFactory.createLiteralByValue(lb, dtype);
        }

        if(range.hasUpperBound()) {
            upperBoundType = range.upperBoundType();

            Object ub = range.upperEndpoint();
            Class<?> ubClass = ub.getClass();

            RDFDatatype dtype = tm.getTypeByClass(ubClass);
            if(dtype == null) {
                throw new IllegalArgumentException("No type mapper entry for " + ubClass);
            }

            upperNode = NodeFactory.createLiteralByValue(ub, dtype);
        }

        ComparableNodeValue lowerNh = lowerNode == null ? null : ComparableNodeValue.wrap(lowerNode);
        ComparableNodeValue upperNh = upperNode == null ? null : ComparableNodeValue.wrap(upperNode);

        Range<ComparableNodeValue> result = RangeSpec.createRange(lowerNh, lowerBoundType, upperNh, upperBoundType);
        return result;
    }

    @Override
    public FacetConstraintCore range(Range<?> range) {
        Range<ComparableNodeValue> nodeRange = toNodeRange(range);
        FacetConstraintCore result = nodeRange(nodeRange);
        return result;
    }
*/

    public FacetConstraintControl regex(String pattern, String flags) {
        Expr expr = new E_Regex(new E_Str(exprVar), pattern, flags);
        FacetConstraintControl result = createConstraint(expr);
        return result;
    }
}

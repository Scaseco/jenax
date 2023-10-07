package org.aksw.facete.v4.impl;

import java.util.Map;

import org.aksw.facete.v3.api.FacetConstraintControl;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;


/**
 * Fluent API wrapper for a {@link FacetConstraintControl}
 */
public class HLFacetConstraintImpl<B>
    implements HLFacetConstraint<B>
{
    protected B parent;
    protected FacetConstraintControl constraint;

    public HLFacetConstraintImpl(B parent, FacetConstraintControl constraint) {
        super();
        this.parent = parent;
        this.constraint = constraint;
    }

    @Override
    public FacetConstraintControl state() {
        return constraint;
    }

    @Override
    public Map<Node, FacetNode> mentionedFacetNodes() {
        // TODO Wrap all paths in the constraints as facet nodes
        throw new UnsupportedOperationException();
    }

    @Override
    public Expr expr() {
        return constraint.expr();
    }

    @Override
    public boolean isActive() {
        return constraint.enabled();
    }

    @Override
    public boolean setActive() {
        constraint.enabled(true);
        return true;
    }

    @Override
    public boolean remove() {
        constraint.unlink();
        return true;
    }

    @Override
    public B parent() {
        return parent;
    }

}

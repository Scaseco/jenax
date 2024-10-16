package org.aksw.jenax.facete.treequery2.impl;

import java.util.Collection;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetConstraintControl;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v4.impl.HLFacetConstraintImpl;
import org.aksw.jenax.arq.util.node.ComparableNodeValue;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.Range;

/**
 * API to access the constraints at the target node reached by a traversal.
 */
public class ConstraintFacade2Impl<T>
    implements ConstraintFacade<T>
{
    protected T parent;
    protected ConstraintApi2Impl<T> constraintApi;

    public ConstraintFacade2Impl(T parent, ConstraintApi2Impl<T> constraintApi) {
        super();
        this.parent = parent;
        this.constraintApi = constraintApi;
    }

    @Override
    public Collection<FacetConstraintControl> list() {
        // constraintApi.listConstraints(facetNode.node);
        // TODO Only list the constraints for the parent facet node

    //    Resource modelRoot = parent.query().modelRoot();
    //    Set<FacetConstraintCore> set =
    //            new ConvertingSet<>(
    //                    new SetFromPropertyValues<>(modelRoot, Vocab.constraint, FacetConstraint.class),
    //                    Converter.from(x -> (FacetConstraintCore)x, y -> (FacetConstraint)y));

    //    return set;

        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<HLFacetConstraint<? extends ConstraintFacade<T>>> listHl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HLFacetConstraint<? extends ConstraintFacade<T>> eq(Node node) {
        FacetConstraintControl fc = constraintApi.eq(node);
        return new HLFacetConstraintImpl<>(this, fc);
    }

    @Override
    public HLFacetConstraint<? extends ConstraintFacade<T>> exists() {
        FacetConstraintControl cc = constraintApi.exists();
        return new HLFacetConstraintImpl<>(this, cc);
    }

    @Override
    public HLFacetConstraint<? extends ConstraintFacade<T>> absent() {
        FacetConstraintControl cc = constraintApi.absent();
        return new HLFacetConstraintImpl<>(this, cc);
    }

    @Override
    public HLFacetConstraint<? extends ConstraintFacade<T>> gt(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HLFacetConstraint<? extends ConstraintFacade<T>> lt(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HLFacetConstraint<? extends ConstraintFacade<T>> neq(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HLFacetConstraint<? extends ConstraintFacade<T>> nodeRange(Range<ComparableNodeValue> range) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HLFacetConstraint<? extends ConstraintFacade<T>> range(Range<?> range) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expr thisAsExpr() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HLFacetConstraint<? extends ConstraintFacade<T>> createConstraint(Expr expr) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HLFacetConstraint<? extends ConstraintFacade<T>> regex(String pattern, String flags) {
        FacetConstraintControl cc = constraintApi.regex(pattern, flags);
        return new HLFacetConstraintImpl<>(this, cc);
    }

    @Override
    public T end() {
        return parent;
    }

}

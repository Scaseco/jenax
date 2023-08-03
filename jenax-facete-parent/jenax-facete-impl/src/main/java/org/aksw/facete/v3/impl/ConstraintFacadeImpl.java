package org.aksw.facete.v3.impl;

import java.util.Collection;
import java.util.Set;

import org.aksw.commons.collections.ConvertingCollection;
import org.aksw.commons.collections.ConvertingSet;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetConstraintCore;
import org.aksw.facete.v3.api.FacetNodeResource;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.aksw.jenax.arq.util.expr.NodeValueUtils;
import org.aksw.jenax.arq.util.node.ComparableNodeValue;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.node.RangeUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

import com.google.common.base.Converter;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

public class ConstraintFacadeImpl<B extends FacetNodeResource>
    implements ConstraintFacade<B>
{
    protected B parent;

    public ConstraintFacadeImpl(B parent) {
        this.parent = parent;
    }

    @Override
    public Collection<FacetConstraintCore> list() {
        // TODO Only list the constraints for the parent facet node

        Resource modelRoot = parent.query().modelRoot();
        Set<FacetConstraintCore> set =
                new ConvertingSet<>(
                        new SetFromPropertyValues<>(modelRoot, Vocab.constraint, FacetConstraint.class),
                        Converter.from(x -> (FacetConstraintCore)x, y -> (FacetConstraint)y));

        return set;
    }


    //@Override

    public HLFacetConstraint<? extends ConstraintFacade<B>> createConstraint(Expr expr) {
        Resource modelRoot = parent.query().modelRoot();
        //Collection<FacetConstraint> set = list();

        FacetConstraint c = modelRoot.getModel().createResource().as(FacetConstraint.class);
        c.expr(expr);


        HLFacetConstraint<ConstraintFacade<B>> result = new HLFacetConstraintImpl<>(this, parent, c);

        //c.expr(new E_Bound(NodeValue.makeNode(parent.state().asNode())));

        return result;
    }
//
//	@Override
//	public ConstraintFacadeImpl<B> addExpr(Expr expr) {
//
//		set.add(c);
//
//		return this;
//	}

    @Override
    public HLFacetConstraint<? extends ConstraintFacade<B>> exists() {
        Expr expr = new E_Bound(thisAsExpr());
        HLFacetConstraint<? extends ConstraintFacade<B>> result = getOrCreateConstraint(expr);
        return result;
    }


    @Override
    public Expr thisAsExpr() {
        Expr result = NodeValue.makeNode(parent.state().asNode());
        return result;
    }

    /**
     * At present we allow a null argument to denote absent values.
     *
     */
    @Override
    public HLFacetConstraint<? extends ConstraintFacade<B>> eq(Node node) {
        HLFacetConstraint<? extends ConstraintFacade<B>> result;

        if(node == null || NodeUtils.nullUriNode.equals(node)) {
            result = absent();
        } else {
            Expr expr = new E_Equals(thisAsExpr(), NodeValue.makeNode(node));
            result = getOrCreateConstraint(expr);
        }

        return result;

//
//		// Check if constraint with that expression already exists
//		List<FacetConstraint> existingEqualConstraints = list().stream().filter(c -> Objects.equals(c.expr(), expr)).collect(Collectors.toList());
//
//		FacetConstraint c;
//		if(existingEqualConstraints.isEmpty()) {
//
//
//			Resource modelRoot = parent.query().modelRoot();
//
//			//Collection<FacetConstraint> set = list(); //new SetFromPropertyValues<>(modelRoot, Vocab.constraint, FacetConstraint.class);
//
//
//			c = modelRoot.getModel().createResource().as(FacetConstraint.class);
//			c.expr(expr);
//		} else {
//			c = existingEqualConstraints.iterator().next();
//		}
//		// TODO Using blank nodes for exprs was a bad idea...
//		// We should just allocate var names
//
//		//c.expr(new E_Equals(new ExprVar((Var)parent.state().asNode()), NodeValue.makeNode(node)));
//
//		//set.add(c);
//
//		HLFacetConstraint<ConstraintFacade<B>> result = new HLFacetConstraintImpl<>(this, parent, c);
//
//		return result;
    }

    @Override
    public HLFacetConstraint<ConstraintFacade<B>> gt(Node node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public HLFacetConstraint<ConstraintFacade<B>> neq(Node node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public B end() {
        return parent;
    }


    @Override
    public HLFacetConstraint<? extends ConstraintFacade<B>> nodeRange(Range<ComparableNodeValue> range) {
        Expr expr = RangeUtils.createExpr(parent.state().asNode(), range);
        HLFacetConstraint<? extends ConstraintFacade<B>> result = getOrCreateConstraint(expr);
        return result;
//
//		Resource modelRoot = parent.query().modelRoot();
//
//		Set<FacetConstraint> set = new SetFromPropertyValues<>(modelRoot, Vocab.constraint, FacetConstraint.class);
//
//		FacetConstraint c = modelRoot.getModel().createResource().as(FacetConstraint.class);
//		Expr expr = RangeUtils.createExpr(parent.state().asNode(), range);
//		c.expr(expr);
//		// TODO Using blank nodes for exprs was a bad idea...
//		// We should just allocate var names
//
//		//c.expr(new E_Equals(new ExprVar((Var)parent.state().asNode()), NodeValue.makeNode(node)));
//		set.add(c);
//
//		return this;
    }


    @Override
    public Collection<HLFacetConstraint<? extends ConstraintFacade<B>>> listHl() {
        Collection<FacetConstraintCore> lowLevel = list();

        ConvertingCollection<HLFacetConstraint<? extends ConstraintFacade<B>>, FacetConstraintCore, ?> result = new ConvertingCollection<>(lowLevel, Converter.from(
            ll -> new HLFacetConstraintImpl<>(this, parent, ll),
            hl -> hl.state()
        ));

        return result;
    }


    @Override
    public HLFacetConstraint<? extends ConstraintFacade<B>> absent() {
        Expr expr = new E_Equals(thisAsExpr(), NodeValueUtils.NV_ABSENT);
        HLFacetConstraint<? extends ConstraintFacade<B>> result = getOrCreateConstraint(expr);
        return result;
    }


    /**
     * Create a range from a range from Java objects (such as Integers) via Jena's Type mapper
     *
     * @param range
     * @return
     */
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
    public HLFacetConstraint<? extends ConstraintFacade<B>> range(Range<?> range) {
        Range<ComparableNodeValue> nodeRange = toNodeRange(range);

        HLFacetConstraint<? extends ConstraintFacade<B>> result = nodeRange(nodeRange);
        return result;
    }

    @Override
    public HLFacetConstraint<? extends ConstraintFacade<B>> regex(String pattern, String flags) {
        HLFacetConstraint<? extends ConstraintFacade<B>> result;

//		if(node == null || NodeUtils.nullUriNode.equals(node)) {
//			result = absent();
//		} else {
        Expr expr = new E_Regex(new E_Str(thisAsExpr()), pattern, flags);
        result = getOrCreateConstraint(expr);
//		}

        return result;


    }


}

package org.aksw.facete.v3.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetConstraintControl;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetNodeResource;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.FacetedQueryResource;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.utils.PathAccessorImpl;
import org.aksw.jena_sparql_api.data_query.api.PathAccessor;
import org.aksw.jena_sparql_api.data_query.impl.PathAccessorUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.expr.Expr;

public class HLFacetConstraintImpl<P>
    implements HLFacetConstraint<P>
{
    protected P parent;
    protected FacetNode facetNode;

    // The expression that can be added and removed from the state
    //protected Expr constraintExpr;

    protected FacetConstraintControl state;

    public HLFacetConstraintImpl(P parent, FacetNode facetNode, FacetConstraintControl state) {
        super();
        this.parent = parent;
        this.facetNode = facetNode;
        this.state = state;
    }

    @Override
    public FacetConstraintControl state() {
        return state;
    }

    @Override
    public Expr expr() {
        Expr result = state.expr();
        return result;
    }
//	/**
//	 *
//	 * Retrieves the expression and substitutes facet node
//	 * references with its simple path
//	 */
//	@Override
//	public String toString() {
//		Expr expr = expr();
//
//		FacetedQueryResource fqr = parent().
//		HLFacetConstraintImpl.mentionedFacetNodes(fqr, expr);
//		FacetNod.mentionedFacetNodes(fqr, baseExpr)
//
//	}
//

    public static Map<Node, BgpNode> mentionedBgpNodes(Model model, Expr baseExpr) {
        PathAccessor<BgpNode> pathAccessor = new PathAccessorImpl(model);

        Map<Node, BgpNode> result = PathAccessorUtils.getPathsMentioned(baseExpr, pathAccessor::tryMapToPath);

        return result;
    }


    public static Map<Node, FacetNode> mentionedFacetNodes(FacetedQuery fq, Expr baseExpr) {
        FacetedQueryResource fqr = fq.as(FacetedQueryResource.class);
//		FacetNodeResource root = facetNode.root().as(FacetNodeResource.class);
        FacetNodeResource fnr  = fq.root().as(FacetNodeResource.class);
//
        BgpNode rootState = fnr.state();
//		//Expr baseExpr = state.expr();
//
//		PathAccessor<BgpNode> pathAccessor = new PathAccessorImpl(rootState.getModel());
//
        Map<Node, BgpNode> paths = mentionedBgpNodes(rootState.getModel(), baseExpr); // PathAccessorImpl.getPathsMentioned(baseExpr, pathAccessor::tryMapToPath);

        Map<Node, FacetNode> result = new LinkedHashMap<>();

        for(Entry<Node, BgpNode> e : paths.entrySet()) {
            result.put(e.getKey(), new FacetNodeImpl(fqr, e.getValue()));
        }
//		accessor.tryMapToPath(node)

        return result;
    }

    public Map<Node, FacetNode> mentionedFacetNodes() {
        FacetedQueryResource fqr = facetNode.query().as(FacetedQueryResource.class);
        Expr baseExpr = state.expr();

        Map<Node, FacetNode> result = mentionedFacetNodes(fqr, baseExpr);

////		FacetNodeResource root = facetNode.root().as(FacetNodeResource.class);
//		FacetNodeResource root = facetNode.query().root().as(FacetNodeResource.class);
//
//		BgpNode rootState = root.state();
//
//		PathAccessor<BgpNode> pathAccessor = new PathAccessorImpl(rootState);
//
//		Map<Node, BgpNode> paths = PathAccessorImpl.getPathsMentioned(baseExpr, pathAccessor::tryMapToPath);
//
//		Map<Node, FacetNode> result = new LinkedHashMap<>();
//
//		for(Entry<Node, BgpNode> e : paths.entrySet()) {
//			result.put(e.getKey(), new FacetNodeImpl(fqr, e.getValue()));
//		}
////		accessor.tryMapToPath(node)
//
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = obj instanceof HLFacetConstraint && Objects.equals(state, ((HLFacetConstraint)obj).state());
        return result;
    }


    @Override
    public boolean isActive() {
        Collection<FacetConstraintControl> items = facetNode.enterConstraints().list();
        boolean result = items.contains(state);
        return result;
    }

    @Override
    public boolean setActive() {
        Collection<FacetConstraintControl> items = facetNode.enterConstraints().list();
        boolean result = items.add(state);
        return result;
        //return this;
    }

    @Override
    public boolean remove() {
        Collection<FacetConstraintControl> items = facetNode.enterConstraints().list();
        boolean result = items.remove(state);
        return result;
        //return this;
    }

    @Override
    public P parent() {
        return parent;
    }

    @Override
    public String toString() {
        // Substitute references in the expression with their respective toString representation
        Expr expr = state.expr();

        Map<Node, FacetNode> map = mentionedFacetNodes();

        Expr e = ExprUtils.applyNodeTransform(expr, n -> {
            Node r;
            FacetNode fn = map.get(n);
            if(fn != null) {
                r = NodeFactory.createLiteral("[" + fn + "]");
            } else {
                r = n;
            }

            return r;
        });

        String result = org.apache.jena.sparql.util.ExprUtils.fmtSPARQL(e);
        return result;
    }
}

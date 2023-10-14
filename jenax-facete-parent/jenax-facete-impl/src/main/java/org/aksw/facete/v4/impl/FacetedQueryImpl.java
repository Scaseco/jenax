package org.aksw.facete.v4.impl;

import java.util.Collection;
import java.util.function.Supplier;

import org.aksw.commons.util.cache.CacheUtils;
import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.TreeQueryNode;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;

import com.google.common.base.Preconditions;
import org.apache.jena.sparql.core.Var;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


/** The entry point is {@link FacetedRelationQuery} */
public class FacetedQueryImpl
    implements FacetedQuery
{
    protected RdfDataSource dataSource;

    protected FacetedRelationQuery relationQuery;
    protected Var baseVar;

    // protected FacetConstraints constraints;
    protected TreeQueryNode focus;

    protected Cache<TreeQueryNode, FacetNode> viewCache = CacheBuilder.newBuilder().maximumSize(1000).build();


//    public static void create() {
//        FacetedRelationQuery frq = FacetedRelationQuery.of(ConceptUtils.createSubjectConcept());
//        FacetedQuery fq = frq.getFacetedQuery();
//        return fq;
//    }

    public FacetedQueryImpl(FacetedRelationQuery relationQuery, Var baseVar, TreeQueryNode focus) {
        super();
        this.relationQuery = relationQuery;
        this.baseVar = baseVar;
        // this.constraints = constraints;
        this.focus = focus;
    }

    public Var getBaseVar() {
        return baseVar;
    }

    public FacetedRelationQuery relationQuery() {
        return relationQuery;
    }

//    public FacetedQueryImpl() {
//        super();
//        TreeQuery treeQuery = new TreeQueryImpl();
//        this.constraints = new FacetConstraints(treeQuery);
//        this.focus = treeQuery.root();
//    }


    FacetNode wrapNode(TreeQueryNode node) {
        return CacheUtils.get(viewCache, node, () -> new FacetNodeImpl(this, node));
    }

    @Override
    public FacetNode root() {
        return wrapNode(focus);
        // return wrapNode(constraints.getTreeQuery().root());
    }

    @Override
    public FacetNode focus() {
        return wrapNode(focus);
    }

    @Override
    public void focus(FacetNode node) {
        Preconditions.checkArgument(node.query() == this, "Facet Node must belong to this query");
        focus = ((FacetNodeImpl)node).node;
    }

    @Override
    public Concept toConcept() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<FacetConstraint> constraints() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FacetedQuery baseConcept(Supplier<? extends Fragment1> conceptSupplier) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FacetedQuery baseConcept(Fragment1 concept) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Fragment1 baseConcept() {
        Var rootVar = relationQuery.varToRoot.inverse().get(focus);
        Fragment r = relationQuery.baseRelation.get();
        return r.project(rootVar).toUnaryRelation();

        // throw new UnsupportedOperationException("Use relationQuery().baseRelation()");
    }

    @Override
    public RdfDataSource dataSource() {
        return dataSource;
    }

    @Override
    public FacetedQuery dataSource(RdfDataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }
}

package org.aksw.facete.v4.impl;

import org.aksw.facete.v3.api.ConstraintApiImpl;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedDataQuery;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.TreeQueryNode;
import org.aksw.facete.v3.api.VarScope;
import org.aksw.facete.v3.impl.FacetedDataQueryImpl;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.facete.treequery2.api.ScopedFacetPath;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;

public class FacetNodeImpl
    implements FacetNode
{
    protected FacetedQueryImpl facetedQuery;

    /** The tree query node that backs path-based views */
    protected TreeQueryNode node;

    public FacetNodeImpl(FacetedQueryImpl facetedQuery, TreeQueryNode node) {
        super();
        this.facetedQuery = facetedQuery;
        this.node = node;
    }

    // TODO Add interface method
    public TreeQueryNode node() {
        return node;
    }

    public FacetNode resolve(FacetPath path) {
        TreeQueryNode target = node.resolve(path);
        return facetedQuery.wrapNode(target);
    }

    @Override
    public FacetNode parent() {
        TreeQueryNode parent = node.getParent();
        FacetNode result = parent == null ? null : facetedQuery.wrapNode(parent);
        return result;
    }

    @Override
    public FacetDirNode fwd() {
        return new FacetDirNodeImpl(this, Direction.FORWARD);
    }

    @Override
    public FacetDirNode bwd() {
        return new FacetDirNodeImpl(this, Direction.BACKWARD);
    }

    @Override
    public FacetedQuery query() {
        return facetedQuery;
    }

    @Override
    public FacetNode chRoot() {
        node.chRoot();
        return this;
    }

    @Override
    public FacetNode chFocus() {
        facetedQuery.focus = node;
        return this;
    }

    @Override
    public FacetNode as(String varName) {
        throw new UnsupportedOperationException("this method will be removed");
    }

    @Override
    public FacetNode as(Var var) {
        throw new UnsupportedOperationException("this method will be removed");
    }

    @Override
    public Var alias() {
        throw new UnsupportedOperationException("this method will be removed");
    }

    public FacetStep reachingStep() {
        FacetPath path = node.getFacetPath();
        FacetStep result = path.getNameCount() == 0 ? null : path.getFileName().toSegment();
        return result;
    }

    @Override
    public Direction reachingDirection() {
        throw new UnsupportedOperationException("use reachingStep()");
    }

    @Override
    public Node reachingPredicate() {
        throw new UnsupportedOperationException("use reachingStep()");
    }

    @Override
    public String reachingAlias() {
        throw new UnsupportedOperationException("use reachingStep()");
    }

    @Override
    public Node targetComponent() {
        throw new UnsupportedOperationException("use reachingStep()");
    }

    @Override
    public Fragment2 getReachingRelation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetNode root() {
        return facetedQuery.root();
    }

    @Override
    public ConstraintFacade<? extends FacetNode> enterConstraints() {
        ConstraintApiImpl constraints = facetedQuery.relationQuery.constraints.getFacade(node);
        return new ConstraintFacadeImpl<FacetNode>(this, constraints);
    }

//    public FacetedRelationQuery createValueQuery2(boolean applySelfConstraints) {
//    	new FacetedRelationQuery(null;)
//    }


    public FacetedDataQuery<RDFNode> createValueQuery(boolean applySelfConstraints) {

        // TODO The base element is missing when running configure - where should it be appended?
        ElementGenerator eltGen = ElementGenerator.configure(facetedQuery);

        // UnaryRelation baseConcept = facetedQuery.baseConcept();
        FacetedRelationQuery relationQuery = facetedQuery.relationQuery();
        String scopeName = relationQuery.getScopeBaseName();
        Var baseVar = facetedQuery.getBaseVar();
        VarScope varScope = VarScope.of(scopeName, baseVar);
        ScopedFacetPath sfp = ScopedFacetPath.of(varScope, node.getFacetPath());

        Fragment baseRelation = facetedQuery.relationQuery().baseRelation.get();


//        Var baseVar = baseConcept.getVar();

        Fragment1 relation = eltGen.getAvailableValuesAt(sfp, applySelfConstraints);

        // Var baseVar = facetedQuery.baseConcept().getVar();
        // Var resolvedVar =  FacetPathMappingImpl.resolveVar(pathMapping, sfp).asVar();


        relation = relation.prependOn(relation.getVars()).with(baseRelation).toUnaryRelation();

        RdfDataSource dataSource = facetedQuery.dataSource();
        FacetedDataQuery<RDFNode> result = new FacetedDataQueryImpl<>(
                dataSource,
                relation.getElement(),
                relation.getVar(),
                null,
                RDFNode.class);

        return result;

    }

    @Override
    public FacetedDataQuery<RDFNode> availableValues() {
        return createValueQuery(false);
    }

    @Override
    public FacetedDataQuery<RDFNode> remainingValues() {
        return createValueQuery(true);
    }
}

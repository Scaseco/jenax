package org.aksw.jenax.facete.treequery2.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.facete.v4.impl.PropertyResolverImpl;
import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.HasSlice;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.QueryContext;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.treequery2.old.NodeQueryOld;
import org.apache.jena.graph.Node;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;

public class RelationQueryImpl
    implements RelationQuery
{
    /** A name given to this relation */
    protected String scopeBaseName;

    /**
     * Supplier for the underlying relation.
     * May by a constant supplier or a dynamic one which e.g. resolves an IRI
     * against a {@link PropertyResolverImpl}.
     */
    // XXX Perhaps the relation should be fixed here? The question is, at which point the properly resolver is invoked.
    //     Probably its best if resolution happens immediately when the client code attempts to traverse a property: then immediately resolve the property to the relation
    //     and add it to this tree structure.
    protected Supplier<Fragment> relationSupplier;

    // XXX We can store a materialized version of the relation here that can be rebuild on demand
    // XXX We can also store a mapping of the variables of the original relation and their scoped versions

    protected QueryContext queryContext;
    protected NodeQuery parent;

    protected Long offset;
    protected Long limit;

    /** A mapping of which variable corresponds to which component of a facet step */
    protected FacetStep reachingStep;

    /** Variables can be declared as having special meaning: SOURCE, TARGET, PREDICATE and GRAPH */
    // XXX With this model we can't declare multiple variables (composite ids) as SOURCE.
    protected Map<Var, Node> varToComponent;

    // protected FacetConstraints constraints;
    // protected FacetConstraints<RootedFacetTraversable<NodeQuery>> facetConstraints;
    protected FacetConstraints<ConstraintNode<NodeQuery>> facetConstraints;

    // protected Map<// Var, RootnODE>
    // protected Map<FacetStep, NodeQuery> children = new LinkedHashMap<>();
    // protected Map<Var, NodeQuery> children = new LinkedHashMap<>();
    // protected Map<FacetStep, NodeQuery> children = new LinkedHashMap<>();
    // we cannot directly link a relation to its children - we need to know on which variable the transition happened
    // So (a) we need a table on the relation (b) we need to add this map to the nodes
    // protected Map<FacetStep, RelationQuery> children = new LinkedHashMap<>();

    protected List<SortCondition> sortConditions = new ArrayList<>();


    protected Map<Var, NodeQuery> roots = new LinkedHashMap<>();

    /** Fields: for each row of this relation, a field may join in additional information.
     *  <pre>
     *  {
     *    thisRelation(v1, ..., vX)
     *    LATERAL {
     *        {
     *          field0 exposes the tuple of the relation itself. further fields connect custom relations.
     *        }
     *      UNION
     *        {
     *          field1(r1, ..., rX) with subList of size X (ri) that joins on this relation.
     *        }
     *      UNION
     *        ...
     *      UNION
     *        {
     *          fieldN
     *        }
     *    }
     *  }
     *  </pre>
     *
     *  A connective is (element, joinVars, visibleVarsForFurtherJoining) - the set of variables of the fragment that join).
     *  This is different from Fragment, which only declares the visible vars. Perhaps, connective could be defined as (fragment, joinVars).
     *
     */
    // protected List<> subRelations;
//    public RelationQueryImpl(Supplier<Relation> relationSupplier, QueryContext queryContext) {
//        this(null, relationSupplier, queryContext);
//    }
//
//    public RelationQueryImpl(RootNode parent, Relation relation, QueryContext queryContext) {
//        this(parent, () -> relation, queryContext);
//    }

    public RelationQueryImpl(String scopeBaseName, NodeQuery parent, Supplier<Fragment> relationSupplier, FacetStep reachingStep, QueryContext queryContext, Map<Var, Node> varToComponent) {
        super();
        this.scopeBaseName = scopeBaseName;
        this.parent = parent;
        this.relationSupplier = relationSupplier;
        this.queryContext = queryContext;
        this.reachingStep = reachingStep;
        this.varToComponent = varToComponent;

        this.facetConstraints = new FacetConstraints<>(ConstraintNode.class);
    }

    @Override
    public String getScopeBaseName() {
        return scopeBaseName;
    }

    @Override
    public NodeQuery getParentNode() {
        return parent;
    }

    @Override
    public Fragment getRelation() {
        return relationSupplier.get();
    }

    @Override
    public List<SortCondition> getSortConditions() {
        return sortConditions;
    }

    /** */
    @Override
    public FacetConstraints<ConstraintNode<NodeQuery>> getFacetConstraints() {
        return facetConstraints;
    }

//    @Override
//    public RelationQuery parent() {
//        return parent;
//    }

    @Override
    public Long offset() {
        return offset;
    }

    @Override
    public RelationQuery offset(Long offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public Long limit() {
        return limit;
    }

    @Override
    public HasSlice limit(Long limit) {
        this.limit = limit;
        return this;
    }

    /**
     * If the relation only defines a single root then return it.
     */
    @Override
    public NodeQueryOld asNode() {
        throw new UnsupportedOperationException();
    }

    /**
     * The path to this relation (a facet path with the last segment having component type TUPLE)
     */
    @Override
    public FacetPath getPath() {
//        FacetPath result;
//        RootNode parent = getParentNode();
//        if (parent == null) {
//            result = FacetPath.newAbsolutePath();
//        } else {
//            result = parent.getPath();
//        }
//
//        return result;
        throw new UnsupportedOperationException("not implemented");
    }

    protected NodeQuery newRoot(Var var) {
        Node component = varToComponent.get(var);
        FacetStep step = component == null ? null : reachingStep.copyStep(component);
        NodeQuery result = new NodeQueryImpl(this, var, step);
        return result;
    }

    @Override
    public List<NodeQuery> roots() {
        Fragment relation = relationSupplier.get();
        List<Var> vars = relation.getVars();
        List<NodeQuery> result = vars.stream()
            .map(var -> roots.computeIfAbsent(var, this::newRoot))
            .collect(Collectors.toList());
        return result;
    }

    // Only return the immediate root or any path with tha variable?!
    @Override
    public NodeQuery nodeFor(Var var) {
        Fragment relation = relationSupplier.get();
        List<Var> vars = relation.getVars();
        if (!vars.contains(var)) {
            throw new NoSuchElementException("Requested a node for variable " + var + " but this one does not exist. Available: " + vars);
        }
        NodeQuery result = roots.computeIfAbsent(var, this::newRoot);
        return result;
    }

    @Override
    public QueryContext getContext() {
        return queryContext;
    }

    @Override
    public Partition partition() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void partitionBy(NodeQueryOld... paths) {
        // TODO Auto-generated method stub
    }

    protected NodeQuery resolveComponent(Node component) {
        Var var = FacetRelationUtils.resolveComponent(component, getRelation());
        NodeQuery result = nodeFor(var);
        return result;
    }

    @Override
    public FacetStep getReachingStep() {
        return reachingStep;
    }

    @Override
    public NodeQuery source() {
        return resolveComponent(FacetStep.SOURCE);
    }

    @Override
    public NodeQuery target() {
        return resolveComponent(FacetStep.TARGET);
    }

    @Override
    public NodeQuery predicate() {
        return resolveComponent(FacetStep.PREDICATE);
    }
}

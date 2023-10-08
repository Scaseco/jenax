package org.aksw.jenax.facete.treequery2.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.util.direction.Direction;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.QueryContext;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprVar;

/**
 * A root node corresponds to a variable of a graph pattern.
 */
public class NodeQueryImpl
    extends NodeQueryBase
    implements NodeQuery
{
    protected RelationQuery relationQuery;
    protected Var var;
    protected FacetStep reachingStep;

    protected Map<FacetStep, RelationQuery> children = new LinkedHashMap<>();
    protected Map<FacetStep, NodeQuery> subPaths = new LinkedHashMap<>();

    protected ConstraintNode<NodeQuery> constraintRoot;

    /** Upon query generation, inject the given graph pattern */
    protected UnaryRelation filterRelation = null;

    public NodeQueryImpl(RelationQueryImpl relationQuery, Var var, FacetStep reachingStep) {
        super();
        this.relationQuery = relationQuery;
        this.var = var;
        this.reachingStep = reachingStep;

        this.constraintRoot = new ConstraintNodeImpl(this, FacetPath.newAbsolutePath());
    }

    @Override
    public NodeQuery getParent() {
        return relationQuery == null ? null : relationQuery.getParentNode();
    }
//    public RootedFacetTraversable<NodeQuery> facets() {
//        return constraintTraversable;
//    }

    @Override
    public NodeQuery setFilterRelation(UnaryRelation filterRelation) {
        this.filterRelation = filterRelation;
        return this;
    }

    @Override
    public UnaryRelation getFilterRelation() {
        return filterRelation;
    }

    @Override
    public ConstraintNode<NodeQuery> constraints() {
        // ConstraintApi2Impl<ConstraintNode<NodeQuery>> result = relationQuery.facetConstraints.getFacade(constraintTraversable);
        return constraintRoot;
    }

    @Override
    public NodeQuery sort(int sortDirection) {
        RelationQuery.doSort(relationQuery(), new ExprVar(var), sortDirection);
        return this;
    }

    @Override
    public int getSortDirection() {
        int result = RelationQuery.getSortDirection(relationQuery(), new ExprVar(var));
        return result;
    }

    @Override
    public Map<FacetStep, RelationQuery> children() {
        return children;
    }

    @Override
    public Collection<NodeQuery> getChildren() {
        return subPaths.values();
    }

//    @Override
//    public RootNode getChild(FacetStep step) {
//        return children.get(step);
//    }

    @Override
    public FacetStep reachingStep() {
        return reachingStep;
    }

    /**
     * The empty path resolves to this node
     */
    @Override
    public NodeQuery resolve(FacetPath facetPath) {
        NodeQuery result;
        if (facetPath.isAbsolute()) {
            FacetPath relativePath = FacetPath.newAbsolutePath().relativize(facetPath);
            RelationQuery rootRelation = relationQuery.root();
            result = rootRelation.target().resolve(relativePath);
        } else {
            if (facetPath.getNameCount() == 0) {
                result = this;
            } else {
                FacetPath startPath = facetPath.subpath(0, 1);
                FacetStep step = startPath.toSegment();
                NodeQuery rn = getOrCreateChild(step);
                FacetPath remainingPath = startPath.relativize(facetPath);
                result = rn.resolve(remainingPath);
            }
        }
        return result;
    }

    @Override
    public NodeQuery getOrCreateChild(FacetStep step) {
        NodeQuery result = subPaths.computeIfAbsent(step, ss -> {
            FacetStep relationStep = FacetStep.of(step.getNode(), step.getDirection(), step.getAlias(), FacetStep.TUPLE);
            RelationQueryImpl tmp = (RelationQueryImpl)children.computeIfAbsent(relationStep, fs -> {
                Node property = fs.getNode();
                Relation baseRelation = relationQuery().getContext().getPropertyResolver().resolve(property);

                if (step.getDirection().equals(Direction.BACKWARD)) {
                    if (baseRelation.getVars().size() != 2) {
                        throw new IllegalArgumentException("Reverse step via " + property + " did not resolve to a binary relation: " + baseRelation);
                    }
                    baseRelation = baseRelation.toBinaryRelation().reverse();
                }

                Var sourceVar = FacetRelationUtils.resolveComponent(FacetStep.SOURCE, baseRelation);
                Var targetVar = relationQuery.target().var();

                QueryContext cxt = relationQuery.getContext();


                String scopeName = cxt.getFieldIdGenerator().next();
                Set<Var> usedVars = cxt.getUsedVars();
                Relation relation = FacetRelationUtils.renameVariables(baseRelation, sourceVar, targetVar, scopeName, usedVars);
                usedVars.addAll(relation.getVarsMentioned());

                Map<Var, Node> varToComponent = FacetRelationUtils.createVarToComponentMap(relation);
                return new RelationQueryImpl(scopeName, this, () -> relation, relationStep, relationQuery.getContext(), varToComponent);
            });

            // We need to get the target node
            Var tgtVar = FacetRelationUtils.resolveComponent(step.getTargetComponent(), tmp.getRelation());
            NodeQuery r = tmp.nodeFor(tgtVar);
            return r;
        });

        return result;
    }

    @Override
    public RelationQuery relationQuery() {
        return relationQuery;
    }

    @Override
    public Var var() {
        return var;
    }

    @Override
    public String toString() {
        return "RootNodeImpl [var=" + var + ", relationQuery=" + relationQuery + "]";
    }

    /**
     * Create a node query rooted in variables ?s of a relation based on an empty graph pattern.
     * The underlying relationQuery is non-null so that projections can be created.
     *
     * @return
     */
    public static NodeQuery newRoot() {
        RelationQuery rq = RelationQuery.of(Vars.s);
        NodeQuery result = rq.nodeFor(Vars.s);
        return result;
        // new RelationQueryImpl("", null, )
        // return new NodeQueryImpl((RelationQueryImpl)RelationQuery.of(new Concept(new ElementGroup(), Vars.s)), Vars.s, null);
    }
}

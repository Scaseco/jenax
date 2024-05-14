package org.aksw.jenax.facete.treequery2.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.util.direction.Direction;
import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.QueryContext;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.api.MappedFragment;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

//public class PropertyGroupImpl
//    implements PropertyGroup
//{
//    protected RelationQuery relationQuery;
//    protected Var var;
//    protected FacetStep reachingStep;
//
//    protected Map<FacetStep, RelationQuery> children = new LinkedHashMap<>();
//    protected Map<FacetStep, NodeQuery> subPaths = new LinkedHashMap<>();
//
//    protected ConstraintNode<NodeQuery> constraintRoot;
//
//    /** Upon query generation, inject the given graph pattern */
//    protected Fragment1 filterRelation = null;
//
//    /**
//     * Extra sparql fragments injected at this node
//     * FIXME Each relation must carry a mapping for how its variables map to facet paths
//     */
//    protected List<MappedFragment<Node>> injectRelations = new ArrayList<>();
//
//    @Override
//    public List<MappedFragment<Node>> getInjectFragments() {
//        return injectRelations;
//    }
//
//    /** reaching step is <fragment:NAME> */
//    public PropertyGroupImpl(RelationQueryImpl relationQuery, Var var, FacetStep reachingStep) {
//        super();
//        this.relationQuery = relationQuery;
//        this.var = var;
//        this.reachingStep = reachingStep;
//
//        // this.constraintRoot = new PropertyGroupImpl(this, FacetPath.newAbsolutePath());
//        this.constraintRoot = new ConstraintNodeImpl(this, FacetPath.newAbsolutePath());
//    }
//
//    @Override
//    public PropertyGroup getParent() {
//        return relationQuery == null ? null : relationQuery.getParentNode();
//    }
//
//    @Override
//    public PropertyGroup setFilterFragment(Fragment1 filterRelation) {
//        this.filterRelation = filterRelation;
//        return this;
//    }
//
//    @Override
//    public Fragment1 getFilterFragment() {
//        return filterRelation;
//    }
//
//    @Override
//    public ConstraintNode<NodeQuery> constraints() {
//        // ConstraintApi2Impl<ConstraintNode<NodeQuery>> result = relationQuery.facetConstraints.getFacade(constraintTraversable);
//        return constraintRoot;
//    }
//
//    @Override
//    public Map<FacetStep, RelationQuery> children() {
//        return children;
//    }
//
//    @Override
//    public Collection<NodeQuery> getChildren() {
//        return subPaths.values();
//    }
//
////    @Override
////    public RootNode getChild(FacetStep step) {
////        return children.get(step);
////    }
//
//    @Override
//    public FacetStep reachingStep() {
//        return reachingStep;
//    }
//
//    /**
//     * The empty path resolves to this node
//     */
//    @Override
//    public PropertyGroup resolve(FacetPath facetPath) {
//        PropertyGroup result;
//        if (facetPath.isAbsolute()) {
//            FacetPath relativePath = FacetPath.newAbsolutePath().relativize(facetPath);
//            RelationQuery rootRelation = relationQuery.root();
//            result = rootRelation.target().resolve(relativePath);
//        } else {
//            if (facetPath.getNameCount() == 0) {
//                result = this;
//            } else {
//                FacetPath startPath = facetPath.subpath(0, 1);
//                FacetStep step = startPath.toSegment();
//                PropertyGroup rn = getOrCreateChild(step);
//                FacetPath remainingPath = startPath.relativize(facetPath);
//                result = rn.resolve(remainingPath);
//            }
//        }
//        return result;
//    }
//
//    @Override
//    public PropertyGroup getOrCreateChild(FacetStep step) {
//        NodeQuery result = subPaths.computeIfAbsent(step, ss -> {
//            FacetStep relationStep = FacetStep.of(step.getNode(), step.getDirection(), step.getAlias(), FacetStep.TUPLE);
//            RelationQueryImpl tmp = (RelationQueryImpl)children.computeIfAbsent(relationStep, fs -> {
//                Node property = fs.getNode();
//                Fragment baseRelation = relationQuery().getContext().getPropertyResolver().resolve(property);
//
//                if (step.getDirection().equals(Direction.BACKWARD)) {
//                    if (baseRelation.getVars().size() != 2) {
//                        throw new IllegalArgumentException("Reverse step via " + property + " did not resolve to a binary relation: " + baseRelation);
//                    }
//                    baseRelation = baseRelation.toFragment2().reverse();
//                }
//
//                Var sourceVar = FacetRelationUtils.resolveComponent(FacetStep.SOURCE, baseRelation);
//                Var targetVar = relationQuery.target().var();
//
//                QueryContext cxt = relationQuery.getContext();
//
//
//                String scopeName = cxt.getFieldIdGenerator().next();
//                Set<Var> usedVars = cxt.getUsedVars();
//                Fragment relation = FacetRelationUtils.renameVariables(baseRelation, sourceVar, targetVar, scopeName, usedVars);
//                usedVars.addAll(relation.getVarsMentioned());
//
//                Map<Var, Node> varToComponent = FacetRelationUtils.createVarToComponentMap(relation);
//                return new PropertyGroupImpl(scopeName, this, () -> relation, relationStep, relationQuery.getContext(), varToComponent);
//            });
//
//            // We need to get the target node
//            Var tgtVar = FacetRelationUtils.resolveComponent(step.getTargetComponent(), tmp.getRelation());
//            NodeQuery r = tmp.nodeFor(tgtVar);
//            return r;
//        });
//
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        return "RootNodeImpl [var=" + var + ", relationQuery=" + relationQuery + "]";
//    }
//
////    @Override
////    public RelationQuery relationQuery() {
////        return relationQuery;
////    }
////
////    @Override
////    public Var var() {
////        return var;
////    }
////
////    @Override
////    public NodeQuery sort(int sortDirection) {
////        RelationQuery.doSort(relationQuery(), new ExprVar(var), sortDirection);
////        return this;
////    }
////
////    @Override
////    public int getSortDirection() {
////        int result = RelationQuery.getSortDirection(relationQuery(), new ExprVar(var));
////        return result;
////    }
//
//}

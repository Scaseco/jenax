package org.aksw.jenax.facete.treequery2.api;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aksw.facete.v3.api.VarScope;
import org.aksw.jenax.arq.util.node.NodeCustom;
import org.aksw.jenax.facete.treequery2.impl.FacetPathMappingImpl;
import org.aksw.jenax.facete.treequery2.impl.OrderNodeImpl;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.api.MappedFragment;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public interface NodeQuery
    extends FacetTraversable<NodeQuery>, HasSlice, Sortable<NodeQuery>
{
    /**
     * Wrap this node as a Jena node so that it can be used as a 'pseudo-variable' in expressions.
     * To substitute NodeQuery references in expressions, apply a node transform using NodeCustom.createNodeTransform(pathTransform).
     */
    default Node asJenaNode() {
        return NodeCustom.of(this);
    }

    /** */
//    getConstraintGroups() {
//
//    }

    /**
     * Sets a graph pattern which is used to 'filter' the values of this node.
     * If the filter pattern can be evaluated to a result set then it results in a set intersection between
     * that result set's column and this node's result set column.
     * If the filter pattern is a filter, then this node's result set is filtered by it.
     */
    NodeQuery setFilterRelation(Fragment1 relation);
    Fragment1 getFilterRelation();


    /**
     * Inject a relation to this node.
     *
     * TODO Consolidate with GraphQlToSparqlConverter; currently the variable mapping is done there
     *
     * @param relation
     * @return
     */
    NodeQuery addInjectRelation(MappedFragment<Node> relation);
    List<MappedFragment<Node>> getInjectRelations();

    @Override
    default FacetPath getFacetPath() {
        RelationQuery relationQuery = relationQuery();
        NodeQuery parentNode = relationQuery.getParentNode();

        FacetPath result;
        if (parentNode == null) {
            result = FacetPath.newAbsolutePath();
        } else {
            FacetPath base = parentNode.getFacetPath();
            FacetStep step = reachingStep();
            if (step == null) {
                throw new NullPointerException();
            }
            // Objects.requireNonNull(step); // Sanity check - only root nodes may return null for steps
            result = base.resolve(step);
        }
        return result;
    }


    // @Override
    default ScopedFacetPath getScopedFacetPath() {
        RelationQuery relationQuery = relationQuery();
        String baseScopeName = relationQuery.getScopeBaseName();
        NodeQuery root = getRoot();
        Var rootVar = root.var();
        FacetPath facetPath = getFacetPath();
        ScopedFacetPath result = ScopedFacetPath.of(baseScopeName, rootVar, facetPath);
        return result;
    }

    default NodeQuery getRoot() {
        RelationQuery relationQuery = relationQuery();
        NodeQuery parentNode = relationQuery.getParentNode();

        NodeQuery result;
        if (parentNode == null) {
            result = this;
        } else {
            result = parentNode.getRoot();
        }
        return result;
    }

    /**
     * A collection of sub-paths of this node
     */
    Collection<NodeQuery> getChildren();


//	default boolean isChildOf(RelationNode relationNode) {
//
//	}

    // FIXME Use a proper interface for the constraints
    ConstraintNode<NodeQuery> constraints();

    RelationQuery relationQuery();
    Var var();
    FacetStep reachingStep();

    @Override
    NodeQuery resolve(FacetPath facetPath);

    Map<FacetStep, RelationQuery> children();

    /** Convenience delegates which set limit/offset on the underlying relation */

    @Override
    default Long offset() {
        RelationQuery relationQuery = relationQuery();
        return relationQuery.offset();
    }

    @Override
    default NodeQuery offset(Long offset) {
        RelationQuery relationQuery = relationQuery();
        relationQuery.offset(offset);
        return this;
    }

    @Override
    default Long limit() {
        RelationQuery relationQuery = relationQuery();
        return relationQuery.limit();
    }

    @Override
    default NodeQuery limit(Long limit) {
        RelationQuery relationQuery = relationQuery();
        relationQuery.limit(limit);
        return this;
    }

    default OrderNode<NodeQuery> orderBy() {
        return new OrderNodeImpl(this);
    }

    /**
     * Compute the VarScope for this nodeQuery.
     * This is essentially the base name when using the NodeQuery as a root for further paths.
     * This is based on the VarScope of this NodeQuery's root, and the facet path between
     * the root and this nodeQuery.
     */
    public static VarScope computeVarScope(NodeQuery nodeQuery) {
        // NodeQuery nodeQuery = constraintNode.getRoot();
        FacetPath facetPath = nodeQuery.getFacetPath(); // TODO The facetPath must affect the scope

        RelationQuery relationQuery = nodeQuery.relationQuery();
        FacetPathMapping pathMapping = relationQuery.getContext().getPathMapping();
        String baseScope = relationQuery.getScopeBaseName();
        String scopeContrib = pathMapping.allocate(facetPath);

        // TODO Probably this should be part of the PathMapping in order to allow for checking for hash clashes
        String finalScope = FacetPathMappingImpl.toString(
                FacetPathMappingImpl.DEFAULT_HASH_FUNCTION.newHasher()
                .putString(baseScope, StandardCharsets.UTF_8)
                .putString(scopeContrib, StandardCharsets.UTF_8)
                .hash());

        // FacetPath constraintPath = constraintNode.getFacetPath();
        VarScope result = VarScope.of(finalScope, nodeQuery.var());
        return result;
    }

    /** Return the ScopedFacetPath that is rooted in this nodeQuery and points to constraintPath */
    public static ScopedFacetPath toScopedFacetPath(NodeQuery nodeQuery, FacetPath constraintPath) {
        VarScope varScope = computeVarScope(nodeQuery);
        ScopedFacetPath scopedFacetPath = ScopedFacetPath.of(varScope, constraintPath);
        return scopedFacetPath;
    }

    /** Return the ScopedFacetPath that points from this <b>nodeQuery's root</b> to this nodeQuery */
//    public static ScopedFacetPath getScopedFacetPathFor(NodeQuery nodeQuery) {
//    }

}

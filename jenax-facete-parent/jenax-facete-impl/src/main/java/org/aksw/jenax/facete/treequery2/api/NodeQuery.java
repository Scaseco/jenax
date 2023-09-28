package org.aksw.jenax.facete.treequery2.api;

import java.util.Collection;
import java.util.Map;

import org.aksw.facete.v3.api.NodeFacetPath;
import org.aksw.jenax.facete.treequery2.impl.OrderNodeImpl;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

public interface NodeQuery
    extends FacetTraversable<NodeQuery>, HasSlice
    // extends NodeQuery
{
	/**
	 * Wrap this node as a Jena node so that it can be used as a 'pseudo-variable' in expressions.
	 * To substitute NodeQuery references in expressions, apply a node transform using NodeFacetPath.createNodeTransform(pathTransform).
	 */
    default Node asJenaNode() {
        return NodeFacetPath.of(this);
    }

    default NodeQuery sortAsc() {
        return sort(Query.ORDER_ASCENDING);
    }

    default NodeQuery sortNone() {
        return sort(Query.ORDER_UNKNOW);
    }

    default NodeQuery sortDefault() {
        return sort(Query.ORDER_DEFAULT);
    }

    default NodeQuery sortDesc() {
        return sort(Query.ORDER_DESCENDING);
    }

    /**
     * Updates or adds the first sort condition of this query node's variable in the list of sort conditions
     */
    NodeQuery sort(int sortDirection);

    /** Returns the direction of the first sort condition that matches this query node's variable */
    int getSortDirection();

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
}

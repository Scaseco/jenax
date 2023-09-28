package org.aksw.jenax.treequery2.old;

import java.util.Collection;
import java.util.List;

import org.aksw.jenax.facete.treequery2.api.HasSlice;
import org.aksw.jenax.facete.treequery2.api.OrderNode;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.graph.Node;
import org.apache.jena.query.SortCondition;

public interface NodeQueryOld
    extends HasSlice
{
    RelationQuery getRelation();

    NodeQueryOld getParent();

    default NodeQueryOld getRoot() {
        NodeQueryOld parent = getParent();
        return parent == null ? this : parent.getRoot();
    }

    FacetPath getPath();

    // List<FacetStep> getChildren();
    Collection<NodeQueryOld> getChildren();


    OrderNode order();

    default NodeQueryOld fwd(String property) {
        return getOrCreateChild(FacetStep.fwd(property));
    }

    default NodeQueryOld fwd(Node property) {
        return getOrCreateChild(FacetStep.fwd(property));
    }

    default NodeQueryOld bwd(String property) {
        return getOrCreateChild(FacetStep.bwd(property));
    }

    default NodeQueryOld bwd(Node property) {
        return getOrCreateChild(FacetStep.bwd(property));
    }

    /** Returns null if there is no child reachable with the given step. */
    NodeQueryOld getChild(FacetStep step);
    NodeQueryOld getOrCreateChild(FacetStep step);

    // NodeQuery getSubQuery(); // Filter the set of resources

    List<SortCondition> getSortConditions();

    // Issue: Limit and offset only generally make sense for relations but not for individual variables of a relation
    // a) maybe navigate from a node to the underlying relation such as queryNode().getRelation().limit() ?
    // b) navigate from the relation to the node - relationNode().limit().getTargetNode()
    // Well, actually the limit / offset methods of this node could just be shortcuts for getRelation().limit()

    /**
     * Convenience method to set the offset on the underlying relation.
     */
    @Override
    default NodeQueryOld offset(Long offset) {
        getRelation().offset(offset);
        return this;
    }

    @Override
    default Long offset() {
        return getRelation().offset();
    }

    /**
     * Convenience method to set the limit on the underlying relation.
     */
    @Override
    default NodeQueryOld limit(Long limit) {
        getRelation().limit(limit);
        return this;
    }

    @Override
    default Long limit() {
        return getRelation().limit();
    }
}

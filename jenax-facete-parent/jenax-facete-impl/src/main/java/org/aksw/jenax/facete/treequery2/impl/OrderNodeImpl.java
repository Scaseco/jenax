package org.aksw.jenax.facete.treequery2.impl;

import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.OrderNode;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;

public class OrderNodeImpl
    extends RootedFacetTraversableBase<NodeQuery, OrderNode<NodeQuery>>
    implements OrderNode<NodeQuery>
{
    public OrderNodeImpl(NodeQuery startNode) {
        this(startNode, FacetPath.newAbsolutePath());
    }

    public OrderNodeImpl(NodeQuery startNode, FacetPath facetPath) {
        super(startNode, facetPath);
    }

    @Override
    public OrderNode<NodeQuery> getParent() {
        FacetPath parentPath = facetPath.getParent();
        return parentPath == null ? null : new OrderNodeImpl(root, parentPath);
    }

    @Override
    public OrderNode<NodeQuery> getOrCreateChild(FacetStep step) {
        return new OrderNodeImpl(root, facetPath.resolve(step));
    }

    @Override
    public NodeQuery asc() {
        return sort(Query.ORDER_ASCENDING);
    }

    @Override
    public NodeQuery desc() {
        return sort(Query.ORDER_DESCENDING);
    }

    protected NodeQuery sort(int sortDirection) {
        // FacetPath facetPath = traversalNode.getFacetPath();
        NodeQuery tgt = root.resolve(facetPath);
        tgt.relationQuery().getSortConditions().add(new SortCondition(tgt.asJenaNode(), sortDirection));
        return root;
    }
}

package org.aksw.jenax.facete.treequery2.impl;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.jenax.arq.util.node.NodeCustom;
import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprLib;

/**
 * This implementation features support for traversals along facet paths
 * as well as setting constraints on the paths.
 */
public class ConstraintNodeImpl
    extends RootedFacetTraversableBase<NodeQuery, ConstraintNode<NodeQuery>>
    implements ConstraintNode<NodeQuery>
{
    public ConstraintNodeImpl(NodeQuery root, FacetPath path) {
        super(root, path);
    }

    @Override
    public ConstraintNode<NodeQuery> getParent() {
        FacetPath parentPath = facetPath.getParent();
        return parentPath == null ? null : new ConstraintNodeImpl(root, parentPath);
    }

    @Override
    public ConstraintNodeImpl getOrCreateChild(FacetStep step) {
        FacetPath newPath = facetPath.resolve(step);
        return new ConstraintNodeImpl(root, newPath);
    }

    @Override
    public ConstraintFacade<ConstraintNode<NodeQuery>> enterConstraints() {
        FacetConstraints<ConstraintNode<NodeQuery>> facetConstraints = root.relationQuery().getFacetConstraints();
        ConstraintApi2Impl<ConstraintNode<NodeQuery>> constraintView = new ConstraintApi2Impl<>(facetConstraints, this);
        return new ConstraintFacade2Impl<>(this, constraintView);
    }

    /**
     * Wrap this node as a Jena node so that it can be used as a 'pseudo-variable' in expressions.
     * To substitute NodeQuery references in expressions, apply a node transform using NodeFacetPath.createNodeTransform(pathTransform).
     */
    public Node asJenaNode() {
        return NodeCustom.of(this);
    }

    @Override
    public ConstraintNode<NodeQuery> sort(int sortDirection) {
        RelationQuery relationQuery = getRoot().relationQuery();
        RelationQuery.doSort(relationQuery, ExprLib.nodeToExpr(asJenaNode()), sortDirection);
        return this;
    }

    @Override
    public int getSortDirection() {
        RelationQuery relationQuery = getRoot().relationQuery();
        int result = RelationQuery.getSortDirection(relationQuery, ExprLib.nodeToExpr(asJenaNode()));
        return result;
    }
}

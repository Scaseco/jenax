package org.aksw.facete.v4.impl;

import java.util.Map;

import org.aksw.facete.v3.api.FacetMultiNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.TreeQueryNode;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

public class FacetMultiNodeImpl
    implements FacetMultiNode
{
    protected FacetDirNodeImpl parent;
    protected Resource property;
    protected Node component;

    public FacetMultiNodeImpl(FacetDirNodeImpl parent, Resource property, Node component) {
        super();
        this.parent = parent;
        this.property = property;
        this.component = component;
    }

    @Override
    public FacetNode viaAlias(String alias) {
        FacetStep step = new FacetStep(property.asNode(), parent.direction.isForward(), alias, component);
        TreeQueryNode node = parent.parent.node;
        FacetNode result = parent.parent.facetedQuery.wrapNode(node.resolve(FacetPath.newRelativePath(step)));
        return result;
    }

    @Override
    public Map<String, FacetNode> list() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasMultipleReferencedAliases() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(FacetNode facetNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remainingValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void availableValues() {
        throw new UnsupportedOperationException();
    }
}

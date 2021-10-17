package org.aksw.jenax.arq.aggregation;

import org.aksw.jenax.analytics.core.ObjectQuery;
import org.apache.jena.graph.Node;

public class RootedQueryImpl
    implements RootedQuery
{
    protected Node rootNode;
    protected ObjectQuery objectQuery;

    public RootedQueryImpl(Node rootNode, ObjectQuery objectQuery) {
        super();
        this.rootNode = rootNode;
        this.objectQuery = objectQuery;
    }

    @Override
    public Node getRootNode() {
        return rootNode;
    }

    @Override
    public ObjectQuery getObjectQuery() {
        return objectQuery;
    }
}

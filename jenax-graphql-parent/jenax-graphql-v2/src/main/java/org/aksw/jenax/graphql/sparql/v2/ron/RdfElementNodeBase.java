package org.aksw.jenax.graphql.sparql.v2.ron;

import java.util.Objects;

import org.apache.jena.graph.Node;

public abstract class RdfElementNodeBase
    implements RdfElementNode
{
    protected Node internalId;
    protected ParentLink parentLink;

    public RdfElementNodeBase(Node internalId) {
        super();
        this.internalId = Objects.requireNonNull(internalId);
    }

    @Override
    public Node getInternalId() {
        return internalId;
    }

    @Override
    public ParentLink getParent() {
        return parentLink;
    }

    void setParent(ParentLink parentLink) {
        this.parentLink = parentLink;
    }
}

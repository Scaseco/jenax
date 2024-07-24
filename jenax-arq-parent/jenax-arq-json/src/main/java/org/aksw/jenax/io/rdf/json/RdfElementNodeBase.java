package org.aksw.jenax.io.rdf.json;

import java.util.Objects;

import org.apache.jena.graph.Node;

public abstract class RdfElementNodeBase
    implements RdfElementNode
{
    protected Node internalId;

    public RdfElementNodeBase(Node internalId) {
        super();
        this.internalId = Objects.requireNonNull(internalId);
    }

    @Override
    public Node getInternalId() {
        return internalId;
    }
}

package org.aksw.jenax.io.rdf.json;

import java.util.Objects;

import org.apache.jena.graph.Node;

public abstract class RdfElementNodeBase
    implements RdfElement
{
    protected Node node;

    public RdfElementNodeBase(Node node) {
        super();
        this.node = Objects.requireNonNull(node);
    }

    public Node getNode() {
        return node;
    }
}

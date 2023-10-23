package org.aksw.jenax.io.rdf.json;

import org.apache.jena.graph.Node;

/**
 * A literal simply wraps a node. Unlike RdfObject, it cannot have additional properties.
 * It thus represents a leaf in a tree structure.
 */
public class RdfLiteral
    extends RdfElementNodeBase
{
    public RdfLiteral(Node node) {
        super(node);
    }

    @Override
    public <T> T accept(RdfElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

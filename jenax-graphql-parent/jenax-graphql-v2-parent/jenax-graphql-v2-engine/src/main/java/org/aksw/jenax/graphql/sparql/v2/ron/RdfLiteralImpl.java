package org.aksw.jenax.graphql.sparql.v2.ron;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * A literal simply wraps a node. Unlike RdfObject, it cannot have additional properties.
 * It thus represents a leaf in a tree structure.
 */
public class RdfLiteralImpl
    extends RdfElementNodeBase
    implements RdfLiteral
{
    public RdfLiteralImpl(Node internalId) {
        super(internalId);
    }

    public RdfLiteralImpl(Resource internalId) {
        super(internalId.asNode());
    }


    @Override
    public <T> T accept(RdfElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

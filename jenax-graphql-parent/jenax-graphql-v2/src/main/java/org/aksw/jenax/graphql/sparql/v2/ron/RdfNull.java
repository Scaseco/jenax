package org.aksw.jenax.graphql.sparql.v2.ron;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class RdfNull
    extends RdfElementNodeBase
{
    // private static final RdfNull INSTANCE = new RdfNull();

//    public static RdfNull get() {
//        return INSTANCE;
//    }

    public RdfNull() {
        this(NodeFactory.createBlankNode());
    }

    public RdfNull(Node internalId) {
        super(internalId);
    }

    @Override
    public <T> T accept(RdfElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

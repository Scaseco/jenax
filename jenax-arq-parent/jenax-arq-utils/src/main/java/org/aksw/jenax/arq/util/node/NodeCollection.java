package org.aksw.jenax.arq.util.node;

import java.util.Collection;

import org.apache.jena.graph.Node;

public interface NodeCollection
    extends Collection<Node>
{
    public static NodeCollection extractOrNull(Node node) {
        return node != null && node.isLiteral() && node.getLiteralValue() instanceof NodeCollection nc
                ? nc
                : null;
    }
}

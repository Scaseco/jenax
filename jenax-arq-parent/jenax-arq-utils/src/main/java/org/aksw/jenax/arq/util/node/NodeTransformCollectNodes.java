package org.aksw.jenax.arq.util.node;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeTransform;

/**
 * Transformer that does nothing, but collects all encountered nodes
 *
 * @author raven
 */
public class NodeTransformCollectNodes
    implements NodeTransform
{
    public Set<Node> nodes = new LinkedHashSet<Node>();

    @Override
    public Node apply(Node node) {
        nodes.add(node);
        return node;
    }

    public Set<Node> getNodes() {
        return nodes;
    }
}

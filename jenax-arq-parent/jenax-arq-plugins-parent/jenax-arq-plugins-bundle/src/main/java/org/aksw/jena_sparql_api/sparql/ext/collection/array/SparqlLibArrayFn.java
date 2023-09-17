package org.aksw.jena_sparql_api.sparql.ext.collection.array;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.aksw.jenax.arq.util.node.NodeSet;
import org.aksw.jenax.arq.util.node.NodeSetImpl;
import org.apache.jena.graph.Node;

public class SparqlLibArrayFn {

    @IriNs({JenaExtensionArray.NS, JenaExtensionArray.LEGACY_NS})
    public static Node get(NodeList nodes, int index) {
        return nodes.get(index);
    }

    @IriNs({JenaExtensionArray.NS, JenaExtensionArray.LEGACY_NS})
    public static NodeList of(Node... nodes) {
        return new NodeListImpl(Arrays.asList(nodes));
    }

    @IriNs({JenaExtensionArray.NS, JenaExtensionArray.LEGACY_NS})
    public static int size(NodeList nodes) {
        return nodes.size();
    }

    @IriNs({JenaExtensionArray.NS, JenaExtensionArray.LEGACY_NS})
    public static NodeSet toSet(NodeList nodes) {
        return new NodeSetImpl(new LinkedHashSet<>(nodes));
    }
}

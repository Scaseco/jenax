package org.aksw.jena_sparql_api.sparql.ext.collection.array;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.aksw.jenax.arq.util.node.NodeSet;
import org.aksw.jenax.arq.util.node.NodeSetImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;

public class SparqlLibArrayFn {

    @IriNs(JenaExtensionArray.NS)
    @IriNs(value=JenaExtensionArray.LEGACY_NS, deprecated=true)
    public static Node get(NodeList nodes, int index) {
        return nodes.get(index);
    }

    @IriNs(JenaExtensionArray.NS)
    @IriNs(value=JenaExtensionArray.LEGACY_NS, deprecated=true)
    public static Node last(NodeList nodes) {
        int n = nodes.size();
        Node result;
        if (n == 0) {
            throw new ExprEvalException("Attempt to access last element of an empty node list");
        } else {
            result = nodes.get(n - 1);
        }
        return result;
    }

    @IriNs(JenaExtensionArray.NS)
    @IriNs(value=JenaExtensionArray.LEGACY_NS, deprecated=true)
    public static NodeList of(Node... nodes) {
        return new NodeListImpl(Arrays.asList(nodes));
    }

    @IriNs(JenaExtensionArray.NS)
    @IriNs(value=JenaExtensionArray.LEGACY_NS, deprecated=true)
    public static int size(NodeList nodes) {
        return nodes.size();
    }

    @IriNs(JenaExtensionArray.NS)
    @IriNs(value=JenaExtensionArray.LEGACY_NS, deprecated=true)
    public static NodeSet toSet(NodeList nodes) {
        return new NodeSetImpl(new LinkedHashSet<>(nodes));
    }
}

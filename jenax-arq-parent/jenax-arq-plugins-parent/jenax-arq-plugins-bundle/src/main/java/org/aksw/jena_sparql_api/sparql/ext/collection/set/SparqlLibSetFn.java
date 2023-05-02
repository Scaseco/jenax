package org.aksw.jena_sparql_api.sparql.ext.collection.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.aksw.jenax.arq.util.node.NodeSet;
import org.aksw.jenax.arq.util.node.NodeSetImpl;
import org.apache.curator.shaded.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;

public class SparqlLibSetFn {
    @IriNs(JenaExtensionSet.NS)
    public static NodeSet of(Node... nodes) {
        return new NodeSetImpl(new LinkedHashSet<>(Arrays.asList(nodes)));
    }

    @IriNs(JenaExtensionSet.NS)
    public static NodeList toArray(NodeSet nodeSet) {
        return new NodeListImpl(new ArrayList<>(nodeSet));
    }

    @IriNs(JenaExtensionSet.NS)
    public static NodeSet intersection(NodeSet a, NodeSet b) {
        return new NodeSetImpl(Sets.intersection(a, b));
    }

    @IriNs(JenaExtensionSet.NS)
    public static NodeSet difference(NodeSet a, NodeSet b) {
        return new NodeSetImpl(Sets.difference(a, b));
    }

    @IriNs(JenaExtensionSet.NS)
    public static NodeSet symmetricDifference(NodeSet a, NodeSet b) {
        return new NodeSetImpl(Sets.symmetricDifference(a, b));
    }

    @IriNs(JenaExtensionSet.NS)
    public static NodeSet union(NodeSet a, NodeSet b) {
        return new NodeSetImpl(Sets.union(a, b));
    }

    @IriNs(JenaExtensionSet.NS)
    public static boolean contains(NodeSet a, Node b) {
        return a.contains(b);
    }

    @IriNs(JenaExtensionSet.NS)
    public static int size(NodeSet nodeSet) {
        return nodeSet.size();
    }
}

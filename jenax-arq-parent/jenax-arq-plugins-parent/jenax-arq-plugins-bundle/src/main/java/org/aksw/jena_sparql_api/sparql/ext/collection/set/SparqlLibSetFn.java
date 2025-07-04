package org.aksw.jena_sparql_api.sparql.ext.collection.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.aksw.jenax.arq.util.node.NodeSet;
import org.aksw.jenax.arq.util.node.NodeSetImpl;
import org.aksw.jenax.norse.term.collection.NorseTermsSet;
import org.apache.curator.shaded.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;

public class SparqlLibSetFn {
    @IriNs(NorseTermsSet.NS)
    @IriNs(value = JenaExtensionSet.NS, deprecated = true)
    public static NodeSet of(Node... nodes) {
        return new NodeSetImpl(new LinkedHashSet<>(Arrays.asList(nodes)));
    }

    @IriNs(NorseTermsSet.NS)
    @IriNs(value = JenaExtensionSet.NS, deprecated = true)
    public static NodeList toArray(NodeSet nodeSet) {
        return new NodeListImpl(new ArrayList<>(nodeSet));
    }

    @IriNs(NorseTermsSet.NS)
    @IriNs(value = JenaExtensionSet.NS, deprecated = true)
    public static NodeSet intersection(NodeSet a, NodeSet b) {
        return new NodeSetImpl(Sets.intersection(a, b));
    }

    @IriNs(NorseTermsSet.NS)
    @IriNs(value = JenaExtensionSet.NS, deprecated = true)
    public static NodeSet difference(NodeSet a, NodeSet b) {
        return new NodeSetImpl(Sets.difference(a, b));
    }

    @IriNs(NorseTermsSet.NS)
    @IriNs(value = JenaExtensionSet.NS, deprecated = true)
    public static NodeSet symmetricDifference(NodeSet a, NodeSet b) {
        return new NodeSetImpl(Sets.symmetricDifference(a, b));
    }

    @IriNs(NorseTermsSet.NS)
    @IriNs(value = JenaExtensionSet.NS, deprecated = true)
    public static NodeSet union(NodeSet a, NodeSet b) {
        return new NodeSetImpl(Sets.union(a, b));
    }

    @IriNs(NorseTermsSet.NS)
    @IriNs(value = JenaExtensionSet.NS, deprecated = true)
    public static boolean contains(NodeSet a, Node b) {
        return a.contains(b);
    }

    @IriNs(NorseTermsSet.NS)
    @IriNs(value = JenaExtensionSet.NS, deprecated = true)
    public static int size(NodeSet nodeSet) {
        return nodeSet.size();
    }
}

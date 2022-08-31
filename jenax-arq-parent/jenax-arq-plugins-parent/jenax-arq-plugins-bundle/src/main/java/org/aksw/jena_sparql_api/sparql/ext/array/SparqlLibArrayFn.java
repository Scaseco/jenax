package org.aksw.jena_sparql_api.sparql.ext.array;

import java.util.Arrays;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.apache.jena.graph.Node;

public class SparqlLibArrayFn {

    @IriNs(JenaExtensionArray.NS)
    public static Node get(NodeList nodes, int index) {
        return nodes.get(index);
    }

    @IriNs(JenaExtensionArray.NS)
    public static NodeList of(Node... nodes) {
        return new NodeListImpl(Arrays.asList(nodes));
    }

}

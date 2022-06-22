package org.aksw.jena_sparql_api.sparql.ext.array;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.util.node.NodeList;
import org.apache.jena.graph.Node;

public class SparqlLibArrayFn {

    @IriNs(JenaExtensionArray.NS)
    public static Node get(NodeList nodes, int index) {
        return nodes.get(index);
    }
}

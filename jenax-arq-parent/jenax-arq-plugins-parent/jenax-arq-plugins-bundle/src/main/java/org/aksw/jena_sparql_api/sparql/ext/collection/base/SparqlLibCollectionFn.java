package org.aksw.jena_sparql_api.sparql.ext.collection.base;

import java.util.LinkedHashSet;

import org.aksw.jenax.annotation.reprogen.DefaultValue;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.util.node.NodeCollection;
import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.aksw.jenax.arq.util.node.NodeSet;
import org.aksw.jenax.arq.util.node.NodeSetImpl;
import org.apache.jena.graph.Node;

public class SparqlLibCollectionFn {

    @IriNs(JenaExtensionCollection.NS)
    public static int size(NodeCollection nodes) {
        return nodes.size();
    }

    @IriNs(JenaExtensionCollection.NS)
    public static NodeSet toSet(NodeCollection nodes) {
        NodeSet result = nodes == null
                ? null
                : nodes instanceof NodeSet
                    ? (NodeSet)nodes
                    : new NodeSetImpl(new LinkedHashSet<>(nodes));
        return result;
    }

    @IriNs(JenaExtensionCollection.NS)
    public static NodeList toArray(NodeCollection nodes) {
        NodeList result = nodes == null
                ? null
                : nodes instanceof NodeList
                    ? (NodeList)nodes
                    : NodeListImpl.copyOf(nodes);
        return result;
    }

    @IriNs(JenaExtensionCollection.NS)
    public static Node unwrapSingle(Node node, @DefaultValue("false") boolean recursive) {
        Node current = node;
        do {
            if (!node.isLiteral()) {
                break;
            }

            Object value = node.getLiteralValue();
            if(!(value instanceof NodeCollection)) {
                break;
            }

            NodeCollection c = (NodeCollection)value;
            if (c.size() != 1) {
                break;
            }

            current = c.iterator().next();
        } while (recursive);

        return current;
    }

}

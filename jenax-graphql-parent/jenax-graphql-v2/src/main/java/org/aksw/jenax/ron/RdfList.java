package org.aksw.jenax.ron;

import java.util.Map.Entry;

import org.apache.jena.graph.Node;

public interface RdfList
    extends RdfElementNode, Iterable<Entry<Node, RdfElement>>
{

    interface RdfListEntry {
        Node getNode();
        RdfElement getElement();
    }

}

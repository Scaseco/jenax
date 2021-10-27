package org.aksw.jena_sparql_api.update;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.google.common.collect.AbstractIterator;

public class IteratorQuadsFromNodeToGraph
    extends AbstractIterator<Quad>
{
    private Iterator<Entry<Node, Graph>> itGraphs;

    private Entry<Node, Graph> currentEntry;
    private ExtendedIterator<Triple> itTriples;


    public IteratorQuadsFromNodeToGraph(Iterator<Entry<Node, Graph>> itGraphs) {
        this.itGraphs = itGraphs;
    }

    @Override
    protected Quad computeNext() {
        while(itTriples == null || !itTriples.hasNext()) {
            for(;;) {
                if(itGraphs.hasNext()) {
                    currentEntry = itGraphs.next();
                    break;

//                    if(currentEntry == null) {
//                        continue;
//                    }
                } else {
                    return endOfData();
                }
            }

            itTriples = currentEntry.getValue().find(Node.ANY,Node.ANY, Node.ANY);
        }

        Node g = currentEntry.getKey();
        Triple triple = itTriples.next();
        Quad result = new Quad(g, triple);
        return result;
    }

    public static IteratorQuadsFromNodeToGraph create(Map<Node, Graph> nodeToGraph) {
        IteratorQuadsFromNodeToGraph result = new IteratorQuadsFromNodeToGraph(nodeToGraph.entrySet().iterator());
        return result;
    }

}
package org.aksw.difs.index.api;

import java.util.stream.Stream;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.txn.api.Txn;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;

public interface DatasetGraphIndexPlugin {
    /**
     * Return an estimate for how well this index can serve the given pattern
     *
     * @return A cost estimate for matching the arguments; null if the index cannot match the pattern
     */
    public Float evaluateFind(Node s, Node p, Node o);

    /**
     * If the result of {{@link #evaluateFind(Node, Node, Node)} is non-null then
     * this method is expected to yield an iterator of the graph nodes which may contain
     * triples matching the arguments.
     *
     * @param s
     * @param p
     * @param o
     * @return
     */
    // public Iterator<Node> listGraphNodes(Node s, Node p, Node o);

    /** Return a stream of resource keys for which there may be graphs
     *  potentially containing matching triples */
    public Stream<Path<String>> listGraphNodes(Txn txn, DatasetGraph dg, Node s, Node p, Node o);

    public void add(Txn txn, DatasetGraph dg, Node g, Node s, Node p, Node o);
    public void delete(Txn txn, DatasetGraph dg, Node g, Node s, Node p, Node o);
}


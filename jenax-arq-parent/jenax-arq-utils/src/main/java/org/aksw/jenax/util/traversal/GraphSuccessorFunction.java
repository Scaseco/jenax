package org.aksw.jenax.util.traversal;

import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.common.collect.Streams;

/**
 * An interface for traversal of all of a node's successors w.r.t. an
 * RDF {@link Graph}.
 *
 * @author raven
 *
 */
public interface GraphSuccessorFunction {
    Stream<Node> apply(Graph graph, Node node);

    /**
     * Create a function that yields for a given Graph and Node a Stream of successors
     * based on a predicate and direction.
     *
     * @param predicate
     * @param isForward
     * @return
     */
    public static GraphSuccessorFunction create(Node predicate, boolean isForward) {
        return isForward
                ? (graph, node) -> Streams.stream(graph.find(node, predicate, Node.ANY)
                        .mapWith(Triple::getObject))
                : (graph, node) -> Streams.stream(graph.find(Node.ANY, predicate, node)
                        .mapWith(Triple::getSubject));
    }

}

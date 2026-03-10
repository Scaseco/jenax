package org.aksw.shellgebra.unused.algebra.dag;

import org.jgrapht.Graph;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class MainPlaygroundDag {
    public static void main(String[] args) {
        // Create a DAG where each node is a StreamNode
        DirectedAcyclicGraph<StreamNode, StreamEdge> dag =
            new DirectedAcyclicGraph<>(StreamNode.vertexSupplier(), StreamEdge::new, false);

        dag.addVertex(new StreamNode("foobar"));

        // Create nodes
        StreamNode a = dag.addVertex();
        StreamNode b = dag.addVertex();
        StreamNode c = dag.addVertex();

        // Add metadata
        a.getMetadata().put("type", "source");
        b.getMetadata().put("type", "intermediate");
        c.getMetadata().put("type", "sink");

        // Add edges
        StreamEdge streamEdge = dag.addEdge(a, b);


        dag.addEdge(b, c);


        // Print graph
        System.out.println(dag);
        System.out.println(streamEdge.getSource() + "->" + streamEdge.getTarget());

        Graph<StreamNode, StreamEdge> reversed = new AsSubgraph<>(
                new EdgeReversedGraph<>(dag),
                dag.vertexSet()
            );

        TopologicalOrderIterator<StreamNode, StreamEdge> it = new TopologicalOrderIterator<>(reversed);
        while (it.hasNext()) {
            StreamNode item = it.next();
            System.out.println(item);
        }
    }

//  public static <V, E> Graph<V, E> createOrderedDAG(Supplier<E> edgeSupplier) {
//  // Create an empty directed graph with edge supplier and custom backing maps
//  DefaultDirectedGraph<V, E> baseGraph = new DefaultDirectedGraph<>(
//      null,                  // no vertex supplier
//      edgeSupplier,
//      false
//  );
//
//  // Set custom data structures to preserve order
//  baseGraph.setVertexSupplier(null); // optional, for newVertex()
//  baseGraph.setVertexMapFactory(LinkedHashMap::new);      // vertex to edge map
//  baseGraph.setEdgeMapFactory(LinkedHashMap::new);        // edge to vertex map
//  baseGraph.setEdgeSetFactory(LinkedHashSet::new);        // edge sets in order
//
//  // Wrap in builder to enforce acyclic constraint
//  return new GraphBuilder<>(baseGraph)
//      .addConstraint(GraphBuilder.ACYCLIC)
//      .buildGraph();
//}

}

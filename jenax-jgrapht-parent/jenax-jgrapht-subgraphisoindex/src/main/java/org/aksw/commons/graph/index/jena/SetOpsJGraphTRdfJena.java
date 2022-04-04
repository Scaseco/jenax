package org.aksw.commons.graph.index.jena;

import java.util.function.Function;

import org.aksw.commons.graph.index.jgrapht.SetOpsJGraphTBase;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.SimpleGraph;

import com.google.common.base.MoreObjects;

public class SetOpsJGraphTRdfJena
    extends SetOpsJGraphTBase<Node, Triple, Graph<Node, Triple>>
{
    public static final SetOpsJGraphTRdfJena INSTANCE = new SetOpsJGraphTRdfJena();

    @Override
    public Graph<Node, Triple> createNew() {
        return new SimpleGraph<>(Triple.class);
    }

    @Override
    protected Triple transformEdge(Triple edge, Function<Node, Node> nodeTransform) {
        //NodeTransform tmp = (node) -> nodeTransform.apply(node);
        NodeTransform tmp = (node) -> MoreObjects.firstNonNull(nodeTransform.apply(node), node);
        Triple result = NodeTransformLib.transform(tmp, edge);

//        System.out.println("Transformed " + edge);
//        System.out.println("  Into " + result);

        return result;
    }

    @Override
    public Graph<Node, Triple> intersect(Graph<Node, Triple> baseGraph, Graph<Node, Triple> removalGraph) {
        Graph<Node, Triple> result = new AsSubgraph<>(baseGraph, removalGraph.vertexSet(), removalGraph.edgeSet());

        //Materialize the intersection
        //Graph<Node, Triple> tmp = createNew();
        //Graphs.addGraph(tmp, result);
        //result = tmp

        return result;
    }
}

package org.aksw.commons.graph.index.jena;

import java.util.function.Function;

import org.aksw.commons.graph.index.core.SetOps;
import org.aksw.jenax.arq.util.triple.GraphIsoMapImpl;
import org.aksw.jenax.arq.util.triple.GraphVarImpl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.graph.compose.Intersection;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;

import com.google.common.collect.BiMap;

/**
 *
 *
 * @author raven
 *
 */
public class SetOpsGraphJena
    implements SetOps<Graph, Node>
{
    public static SetOpsGraphJena INSTANCE = new SetOpsGraphJena();

    @Override
    public Graph createNew() {
        return new GraphVarImpl();
        //return GraphFactory.createDefaultGraph();
    }

    @Override
    public Graph applyIso(Graph set, BiMap<Node, Node> iso) {
        //Graph result = transformItems(set, iso::get);
        Graph result = new GraphIsoMapImpl(set, iso);
        return result;
    }

    @Override
    public int size(Graph set) {
        int result = set.size();
        return result;
    }

    @Override
    public Graph difference(Graph baseSet, Graph removalSet) {
        Graph result = new Difference(baseSet, removalSet);
        return result;
    }

    @Override
    public Graph intersect(Graph a, Graph b) {
        Graph result = new Intersection(a, b);
        return result;
    }

    @Override
    public Graph transformItems(Graph graph, Function<Node, Node> nodeTransform) {
        NodeTransform tmp = (node) -> nodeTransform.apply(node);
        Graph result = createNew();
        graph.find(null, null, null).forEachRemaining(t -> {
        //graph.find(Node.ANY, Node.ANY, Node.ANY).forEachRemaining(t -> {
            Triple u = NodeTransformLib.transform(tmp, t);
            result.add(u);
        });
        return result;
    }

    @Override
    public Graph union(Graph a, Graph b) {
        Graph result = new Union(a, b);
        return result;
    }

    @Override
    public boolean isEmpty(Graph s) {
        boolean result = s.isEmpty();
        return result;
    }

}

package org.aksw.jenax.sparql.rx.op;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.quad.DatasetUtils;
import org.aksw.jenax.sparql.query.rx.StreamUtils.ConsecutiveNamedGraphMergerCore;
import org.aksw.jenax.sparql.relation.dataset.GraphNameAndNode;
import org.aksw.jenax.sparql.relation.dataset.NodesInDataset;
import org.aksw.jenax.sparql.relation.dataset.NodesInDatasetImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;

public class ConsecutiveGraphMergerMergerForResourceInDataset
    extends ConsecutiveNamedGraphMergerCore<NodesInDataset>
{
    protected Map<Node, List<Node>> graphToNodes= new HashMap<>();

    public Optional<NodesInDataset> accept(NodesInDataset grid) {
        Dataset dataset = grid.getDataset();

        for(GraphNameAndNode e : grid.getGraphNameAndNodes()) {
            Node g = NodeFactory.createURI(e.getGraphName());

            // Ensure that the referenced graphs are actually mentioned in the datasets,
            // otherwise they will fill up the memory but will never be returned
            boolean isNodeInGraph = DatasetUtils.containsDefaultOrNamedModel(dataset, g);

            if(isNodeInGraph) {
                graphToNodes
                    .computeIfAbsent(g, x -> new ArrayList<>())
                    .add(e.getNode());
            }
        }

        Optional<NodesInDataset> result = super.accept(dataset);
        return result;
    }

    @Override
    protected NodesInDataset mapResult(Set<Node> readyGraphs, Dataset dataset) {
        Set<GraphNameAndNode> gans = readyGraphs.stream()
            .flatMap(g -> graphToNodes.getOrDefault(g, Collections.emptyList())
                    .stream().map(n -> new GraphNameAndNode(g.getURI(), n)))
            .collect(Collectors.toSet());

        for(Node node : readyGraphs) {
            graphToNodes.remove(node);
        }

        NodesInDataset result = new NodesInDatasetImpl(dataset, gans);

        return result;
    }
}

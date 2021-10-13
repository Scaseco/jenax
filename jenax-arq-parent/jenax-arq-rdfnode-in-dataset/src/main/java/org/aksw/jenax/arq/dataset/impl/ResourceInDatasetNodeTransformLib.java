package org.aksw.jenax.arq.dataset.impl;

import java.util.Optional;

import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.util.transform.NodeTransformLib2;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.graph.NodeTransform;

public class ResourceInDatasetNodeTransformLib {


    /**
     * Rename multiple RDFterms
     *
     * @param old
     * @param renames
     * @return
     */
    public static ResourceInDataset applyNodeTransform(ResourceInDataset old, NodeTransform nodeTransform) {
        String graphName = old.getGraphName();
        Node graphNode = NodeFactory.createURI(graphName);
        Node newGraphNode = Optional.ofNullable(nodeTransform.apply(graphNode)).orElse(graphNode);

        Node n = old.asNode();
        Node newNode = Optional.ofNullable(nodeTransform.apply(n)).orElse(n);

        String g = newGraphNode.getURI();

        Dataset dataset = old.getDataset();
        NodeTransformLib2.applyNodeTransform(nodeTransform, dataset);

        ResourceInDataset result = new ResourceInDatasetImpl(dataset, g, newNode);
        return result;

    }


    public static ResourceInDataset copyWithNodeTransform(ResourceInDataset old, Dataset target, NodeTransform nodeTransform) {
        String graphName = old.getGraphName();
        Node graphNode = NodeFactory.createURI(graphName);
        Node newGraphNode = Optional.ofNullable(nodeTransform.apply(graphNode)).orElse(graphNode);
        String g = newGraphNode.getURI();

        Node n = old.asNode();
        Node newNode = Optional.ofNullable(nodeTransform.apply(n)).orElse(n);

        NodeTransformLib2.copyWithNodeTransform(nodeTransform, old.getDataset(), target);

        ResourceInDataset result = new ResourceInDatasetImpl(target, g, newNode);
        return result;

    }

}

package org.aksw.jenax.arq.dataset.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.dataset.api.RDFNodeInDataset;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.util.quad.DatasetUtils;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

public class RDFNodeInDatasetUtils {

    /** If the rdfNode is based on a GraphView then an appropriate RDFNodeInDataset view is returned;
     * otherwise the rdfNode's model is wrapped as a Dataset.
     *
     * Always creates a fresh Dataset wrapper for an underlying DatasetGraph.
     * */
    public RDFNodeInDataset asRDFNodeInDataset(RDFNode rdfNode) {
        RDFNodeInDataset result;

        Model model = rdfNode.getModel();
        Graph graph = Optional.ofNullable(model).map(Model::getGraph).orElse(null);
        if (graph instanceof GraphView) {
            GraphView gv = (GraphView)graph;
            Node graphNode = gv.getGraphName();
            DatasetGraph dg = gv.getDataset();
            result = RDFNodeInDataset.create(DatasetFactory.wrap(dg), graphNode.getURI(), rdfNode.asNode());
        } else {
            result = RDFNodeInDataset.create(DatasetFactory.wrap(model), Quad.defaultGraphIRI.getURI(), rdfNode.asNode());
        }

        return result;
    }

    /**
     * The returned extended iterator does not yet support removal, as
     * there is no flatMap method.
     * The API is just there to be potentially future proof.
     *
     * @param dataset
     * @param p
     * @return
     */
    public static ExtendedIterator<ResourceInDataset> listResourcesWithProperty(Dataset dataset, Property p) {
        Iterator<String> it = Iterators.concat(
                Collections.singleton(Quad.defaultGraphIRI.getURI()).iterator(),
                dataset.listNames());

        Collection<String> graphNames = Lists.newArrayList(it);

        List<ResourceInDataset> list = graphNames.stream().flatMap(graphName -> {
            Model model = DatasetUtils.getDefaultOrNamedModel(dataset, graphName);
            List<ResourceInDataset> rs = model
                    .listResourcesWithProperty(p)
                    .<ResourceInDataset>mapWith(r ->
                        new ResourceInDatasetImpl(dataset, graphName, r.asNode()))
                    .toList();

            return rs.stream();
        }).collect(Collectors.toList());

        return WrappedIterator.create(list.iterator());
    }
}

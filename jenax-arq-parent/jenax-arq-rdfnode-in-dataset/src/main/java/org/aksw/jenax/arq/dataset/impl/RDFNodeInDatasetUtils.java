package org.aksw.jenax.arq.dataset.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.util.dataset.DatasetUtils;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

public class RDFNodeInDatasetUtils {
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

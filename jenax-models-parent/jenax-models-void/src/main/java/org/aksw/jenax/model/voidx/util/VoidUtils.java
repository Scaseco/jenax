package org.aksw.jenax.model.voidx.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jenax.model.voidx.api.VoidDataset;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.VOID;

public class VoidUtils {
    public static List<VoidDataset> listVoidDatasets(Model model) {
        List<VoidDataset> result;
        try (Stream<VoidDataset> stream = Stream.concat(
                Iter.asStream(model.listResourcesWithProperty(VOID.classPartition)),
                Iter.asStream(model.listResourcesWithProperty(VOID.propertyPartition)))
        .distinct()
        .map(r -> r.as(VoidDataset.class))) {
            result = stream.collect(Collectors.toList());
        }
        return result;
    }
}

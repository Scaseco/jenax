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
        /*
        v:propertyPartition ?l .

        ?l
          v:property ?p ;
          s:objectTypes ?k . # targets are classPartitions for the object types
        */

        List<VoidDataset> result;
        // Find resources with top-level(!) classPartitions and/or propertyPartitions.
        try (Stream<VoidDataset> stream = Stream.concat(
                Iter.asStream(model.listResourcesWithProperty(VOID.classPartition)),
                        // .filterDrop(x -> model.contains(null, VOID.propertyPartition, x))),
                Iter.asStream(model.listResourcesWithProperty(VOID.propertyPartition)
                        .filterDrop(x -> model.contains(null, VOID.classPartition, x))))
        .distinct()
        .map(r -> r.as(VoidDataset.class))) {
            result = stream.collect(Collectors.toList());
        }
        return result;
    }
}

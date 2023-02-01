package org.aksw.jenax.arq.dataset.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;

public class CachePatterns {
    public static List<CachePattern> forNeighborsByPredicate(Node predicate) {
        return forNeigborsByPredicates(Collections.singleton(predicate));
    }

    public static List<CachePattern> forNeigborsByPredicates(Collection<Node> predicates) {
        List<CachePattern> result = predicates.stream().flatMap(p -> Stream.of(
                CachePattern.create(CachePattern.IN, CachePattern.IN, p, Node.ANY),
                CachePattern.create(CachePattern.IN, Node.ANY, p, CachePattern.IN)
        )).collect(Collectors.toList());
        return result;
    }
}

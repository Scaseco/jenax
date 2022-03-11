package org.aksw.jenax.arq.analytics;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.core.SetOverMap;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.jena.graph.Node;

/**
 * Aggregation utilities for Jena Nodes
 *
 * @author Claus Stadler
 *
 */
public class NodeAnalytics {

    public static ParallelAggregator<Node, Entry<Set<String>, Long>, ?> usedDatatypesAndNullCounts() {
        return AggBuilder.inputBroadcast(
            usedDatatypes(),
            nullCount());
    }

    public static ParallelAggregator<Node, Long, ?> nullCount() {
        ParallelAggregator<Node, Long, ?> result =
            AggBuilder.inputFilter(x -> x == null,
                AggBuilder.counting());

        return result;
    }

    public static ParallelAggregator<Node, Set<String>, ?> usedDatatypes() {

        ParallelAggregator<Node, Set<String>, ?> result = AggBuilder.inputTransform(node -> NodeUtils.getDatatypeIri(node),
            AggBuilder.inputFilter(Objects::nonNull,
                AggBuilder.collectionSupplier(() -> (Set<String>)new HashSet<String>())));

        return result;
    }


//	public static ParallelAggregator<Node, Multiset<String>, ?> usedDatatypesWithCounts() {
//
//		ParallelAggregator<Node, Multiset<String>, ?> result = AggBuilder.inputTransform(node -> NodeUtils.getDatatypeIri(node),
//			AggBuilder.inputFilter(Objects::nonNull,
//				AggBuilder.collectionSupplier(() -> (Multiset<String>)LinkedHashMultiset.<String>create())));
//
//		return result;
//	}

    public static ParallelAggregator<Node, Set<String>, ?> usedPrefixes(int targetSize) {
        ParallelAggregator<Node, Set<String>, ?> result =
            AggBuilder.inputFilter(Node::isURI,
                AggBuilder.inputTransform(Node::getURI,
                    AggBuilder.naturalAccumulator(() -> new PrefixAccumulator(targetSize))));

        return result;
    }

    /**
     * Accumulate only prefixes that appear in the reference set.
     * Filters out any prefix not part of the reference set.
     *
     * @param prefixMap A prefix map prefix -> iri
     * @param targetSize
     * @return
     */
    public static ParallelAggregator<Node, Map<String, String>, ?> usedPrefixes(Map<String, String> prefixMap) {
        // Internally invert the mapping to iri -> prefix
        PatriciaTrie<String> trie = new PatriciaTrie<>();
        prefixMap.forEach((p, i) -> trie.put(i, p));

//        if ((prefixMap instanceof PatriciaTrie)) {
//            trie = (PatriciaTrie<String>)prefixMap;
//        } else {
//            trie = new PatriciaTrie<>(prefixMap);
//        }

        // Create an aggregator that returns the subset of the prefix map
        // that is in use w.r.t. the encountered IRIs.
        ParallelAggregator<Node, SetOverMap<String, String>, ?> tmp =
            AggBuilder.inputFilter((Node n) -> n != null && n.isURI(),
                AggBuilder.inputTransform((Node node) -> {
                    String uri = node.getURI();
                    Entry<String, String> cand = trie.select(uri);
                    Entry<String, String> e = uri.startsWith(cand.getKey())
                        ? cand
                        : null;
                    return e;
                },
                AggBuilder.inputFilter((Entry<String, String> e) -> e != null,
                    AggBuilder.mapSupplier(() -> new PatriciaTrie<String>()))));

        // Post process the result:
        // Extract the map and invert it again so we end up with prefix -> iri
        ParallelAggregator<Node, Map<String, String>, ?> result =
            AggBuilder.outputTransform(tmp, (SetOverMap<String, String> som) -> {
                Map<String, String> r = som.getMap().entrySet()
                        .stream()
                        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));
                return r;
            });

        return result;
    }

}

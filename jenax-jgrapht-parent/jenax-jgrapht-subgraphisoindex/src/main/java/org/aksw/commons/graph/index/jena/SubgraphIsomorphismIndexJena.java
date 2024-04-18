package org.aksw.commons.graph.index.jena;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.tagmap.TagMap;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndexFlat;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndexImpl;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndexTagBased;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.util.NodeCmp;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.jgrapht.Graph;

import com.google.common.collect.BiMap;
import com.google.common.collect.Streams;

public class SubgraphIsomorphismIndexJena {

    // TODO Move to util class
    public static <K> SubgraphIsomorphismIndex<K, Graph<Node, Triple>, Node> createFlat() {
        SubgraphIsomorphismIndexFlat<K, Graph<Node, Triple>, Node> result =
                new SubgraphIsomorphismIndexFlat<>(
                        SetOpsJGraphTRdfJena.INSTANCE,
                        new IsoMatcherImpl<Node, Triple, Graph<Node, Triple>>(SubgraphIsomorphismIndexJena::createNodeComparator, SubgraphIsomorphismIndexJena::createEdgeComparator));
        return result;
    }


    // TODO Move to util class
    public static <K> SubgraphIsomorphismIndex<K, Graph<Node, Triple>, Node> create() {
        SubgraphIsomorphismIndexImpl<K, Graph<Node, Triple>, Node, Node> result =
                new SubgraphIsomorphismIndexImpl<>(
                        SetOpsJGraphTRdfJena.INSTANCE,
                        SubgraphIsomorphismIndexJena::extractGraphTags,
                        NodeCmp::compareRDFTerms,
                        new IsoMatcherImpl<Node, Triple, Graph<Node, Triple>>(SubgraphIsomorphismIndexJena::createNodeComparator, SubgraphIsomorphismIndexJena::createEdgeComparator));
        return result;
    }

    public static <K> SubgraphIsomorphismIndexTagBased<K, Graph<Node, Triple>, Node, Node> createTagBased(TagMap<K, Node> tagMap) {
        SubgraphIsomorphismIndexTagBased<K, Graph<Node, Triple>, Node, Node> result =
                new SubgraphIsomorphismIndexTagBased<K, Graph<Node, Triple>, Node, Node>(
                        new IsoMatcherImpl<Node, Triple, Graph<Node, Triple>>(SubgraphIsomorphismIndexJena::createNodeComparator, SubgraphIsomorphismIndexJena::createEdgeComparator),
                        SubgraphIsomorphismIndexJena::extractGraphTags,
                        tagMap
                        );
        return result;
    }


    public static Comparator<Node> createNodeComparator(BiMap<? extends Node, ? extends Node> baseIso) {
        Comparator<Node> result = (x, y) -> compareNodes(baseIso, x, y);
        return result;
    }

    public static Comparator<Triple> createEdgeComparator(BiMap<? extends Node, ? extends Node> baseIso) {
        Comparator<Triple> result = (x, y) -> compareNodes(baseIso, x.getPredicate(), y.getPredicate());
        return result;
    }

    public static int compareNodes(BiMap<? extends Node, ? extends Node> baseIso, Node i, Node j) {
        int result = (
                        (i.isVariable() && j.isVariable()) ||
                        (i.isBlank() && j.isBlank() ||
                        Objects.equals(baseIso.get(i), j)))
                ? 0
                : NodeCmp.compareRDFTerms(i, j);

        return result;
    }

    public static Set<Node> extractGraphTags(Graph<Node, Triple> graph) {
        Set<Node> result = graph.edgeSet().stream()
            .flatMap(t -> Arrays.asList(t.getSubject(), t.getPredicate(), t.getObject()).stream())
            .filter(n -> n.isURI() || n.isLiteral())
            .collect(Collectors.toSet());

        return result;
    }

    public static Collection<?> extractGraphTags2(org.apache.jena.graph.Graph graph) {
        // TODO: All nodes does not include predicates
        Set<Node> result = Streams.stream(GraphUtils.allNodes(graph))
                .filter(n -> n.isURI() || n.isLiteral())
                .collect(Collectors.toSet());

        return result;
    }
}

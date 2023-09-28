package org.aksw.jenax.sparql.query.rx;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.dataset.orderaware.DatasetFactoryEx;
import org.aksw.jenax.arq.util.quad.DatasetGraphUtils;
import org.aksw.jenax.arq.util.quad.QuadUtils;
import com.google.common.collect.Sets;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.Quad;

import com.google.common.collect.Lists;

public class StreamUtils {
    /**
     * An encoder that renames a graph if it is encountered on successive encoding requests.
     *
     * @author raven
     *
     */
    public static class QuadEncoderDistinguish {
        protected Set<Node> priorGraphs = Collections.emptySet();

        // Do we need synchronized? Processing should happen in order anyway!
        public Dataset encode(Dataset dataset) {
            Set<Node> now = Sets.newHashSet(dataset.asDatasetGraph().listGraphNodes());
            List<Quad> quads = Lists.newArrayList(dataset.asDatasetGraph().find());

            Set<Node> conflicts = Sets.intersection(priorGraphs, now);
            List<Quad> newQuads = quads.stream()
                    .map(q -> conflicts.contains(q.getGraph()) ? encodeDistinguished(q) : q)
                    .collect(Collectors.toList());

            priorGraphs = now.stream()
                .map(n -> conflicts.contains(n) ? encodeDistinguished(n) : n)
                .collect(Collectors.toSet());

            Dataset result = DatasetFactoryEx.createInsertOrderPreservingDataset(newQuads);
            return result;
            // Rename all graphs in the intersection
        }
    }

    /**
     * Prefixes to distinguishes consecutive different events of the same named graph
     *
     */
    public static final String DISTINGUISHED_PREFIX = "x-distinguished:";
    public static final int DISTINGUISHED_PREFIX_LENGTH = DISTINGUISHED_PREFIX.length();

    public static Node encodeDistinguished(Node g) {
        Node result = g;
        if(g.isURI()) {
            String str = DISTINGUISHED_PREFIX + g.getURI();
            result = NodeFactory.createURI(str);
        }
        return result;
    }

    public static Quad encodeDistinguished(Quad quad) {
        Node g = quad.getGraph();
        Node encoded = encodeDistinguished(g);
        Quad result = encoded == g ? quad : new Quad(encoded, quad.asTriple());

        return result;
    }

    public static Node decodeDistinguished(Node g) {
        Node result = g;
        if(g.isURI()) {
            String str = g.getURI();
            if(str.startsWith(DISTINGUISHED_PREFIX)) {
                result = NodeFactory.createURI(str.substring(DISTINGUISHED_PREFIX_LENGTH));
            }
        }
        return result;
    }

    public static Quad decodeDistinguished(Quad quad) {
        Quad result = quad;
        Node g = quad.getGraph();
        if(g.isURI()) {
            String str = g.getURI();
            if(str.startsWith(DISTINGUISHED_PREFIX)) {
                result = new Quad(NodeFactory.createURI(str.substring(DISTINGUISHED_PREFIX_LENGTH)), quad.asTriple());
            }
        }
        return result;
    }


    /**
     * Stateful collector that merges any consecutive graphs of name
     * contained in the datasets passed to the accept method.
     *
     *
     * @author raven
     *
     */
    public static class ConsecutiveNamedGraphMerger
        extends ConsecutiveNamedGraphMergerCore<Dataset>
    {
        @Override
        protected Dataset mapResult(Set<Node> readyGraphs, Dataset dataset) {
            return dataset;
        }
    }

    public static abstract class ConsecutiveNamedGraphMergerCore<T> {
        protected Map<Node, Set<Quad>> pending = new LinkedHashMap<>();

        public synchronized Optional<T> accept(Dataset dataset) {
            Supplier<Set<Quad>> setSupplier = LinkedHashSet::new;

            Iterator<Quad> it = dataset.asDatasetGraph().find();
            Map<Node, Set<Quad>> index = QuadUtils.partitionByGraph(
                    it,
                    new LinkedHashMap<Node, Set<Quad>>(),
                    setSupplier);

            Set<Node> before = pending.keySet();
            Set<Node> now = index.keySet();

            Set<Node> overlap = Sets.intersection(now, before);
            Set<Node> newGraphs = Sets.difference(now, before);
//					readyGraphs,
//					Sets.difference(now, before));

            for(Node appending : overlap) {
                Set<Quad> tgt = pending.get(appending);
                Set<Quad> src = index.get(appending);
                tgt.addAll(src);
            }

            for(Node newGraph : newGraphs) {
                Set<Quad> src = index.get(newGraph);
                pending.put(newGraph, src);
            }

            // Emit the ready graphs
            Dataset resultDataset = DatasetFactoryEx.createInsertOrderPreservingDataset();
            Set<Node> readyGraphs = new HashSet<>(Sets.difference(before, now));

            for(Node ready : readyGraphs) {
                Set<Quad> quads = pending.get(ready);

                DatasetGraphUtils.addAll(resultDataset.asDatasetGraph(), quads);

                pending.remove(ready);
            }

            T result = readyGraphs.isEmpty()
                    ? null
                    : mapResult(readyGraphs, resultDataset);

            //System.err.println("Pending size " + pending..size());

            return Optional.ofNullable(result);
        }

        protected abstract T mapResult(Set<Node> readyGraphs, Dataset dataset);

        public Optional<T> getPendingDataset() {
            T resultData;
            if(pending.isEmpty()) {
                resultData = null;
            } else {
                Dataset dataset = DatasetFactoryEx.createInsertOrderPreservingDataset();
                for(Collection<Quad> quads : pending.values()) {
                    DatasetGraphUtils.addAll(dataset.asDatasetGraph(), quads);
                }

                resultData = mapResult(pending.keySet(), dataset);
            }
            return Optional.ofNullable(resultData);
        }
    }




    @Deprecated
    public static class QuadEncoderMergeOld {
        protected Dataset pending = DatasetFactory.create();

        public synchronized Dataset accept(Dataset dataset) {
            Set<Node> before = Sets.newHashSet(pending.asDatasetGraph().listGraphNodes());
            Set<Node> now = Sets.newHashSet(dataset.asDatasetGraph().listGraphNodes());

            Set<Node> readyGraphs = Sets.difference(before, now);
            Set<Node> appendings = Sets.union(
                    Sets.intersection(before, now),
                    Sets.difference(now, before));

            for(Node appending : appendings) {
                Graph tgt = pending.asDatasetGraph().getGraph(appending);
                Graph src = dataset.asDatasetGraph().getGraph(appending);

                GraphUtil.addInto(tgt, src);
            }

            // Emit the ready graphs
            Dataset result = DatasetFactory.create();
            for(Node ready : readyGraphs) {
                Graph src = pending.asDatasetGraph().getGraph(ready);
                DatasetGraphUtils.addAll(result.asDatasetGraph(), ready, src);

                pending.asDatasetGraph().removeGraph(ready);
            }

            System.err.println("Pending size " + pending.asDatasetGraph().size());

            return result;
        }

        public Dataset getPendingDataset() {
            return pending;
        }
    }

}

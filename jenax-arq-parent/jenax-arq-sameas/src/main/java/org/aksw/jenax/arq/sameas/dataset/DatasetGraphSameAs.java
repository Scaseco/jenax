package org.aksw.jenax.arq.sameas.dataset;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.dataset.DatasetGraphWrapperFindBase;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphWrapperView;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Traverser;

public class DatasetGraphSameAs
    extends DatasetGraphWrapperFindBase
    implements DatasetGraphWrapperView
{
    // private static final Logger logger = LoggerFactory.getLogger(GraphWithSameAsInference.class);

    protected LoadingCache<Entry<Node, Node>, Set<Triple>> cache;
    protected Set<Node> sameAsPredicates;

    public static final int DFT_MAX_CACHE_SIZE = 10_000;

    public static DatasetGraph wrap(DatasetGraph base) {
        return wrap(base, OWL.sameAs.asNode(), DFT_MAX_CACHE_SIZE);
    }

    public static DatasetGraph wrap(DatasetGraph base, int maxCacheSize) {
        return wrap(base, OWL.sameAs.asNode(), maxCacheSize);
    }

    public static DatasetGraph wrap(DatasetGraph base, Node sameAsPredicate) {
        return new DatasetGraphSameAs(base, Collections.singleton(sameAsPredicate), DFT_MAX_CACHE_SIZE);
    }

    public static DatasetGraph wrap(DatasetGraph base, Node sameAsPredicate, int maxCacheSize) {
        return new DatasetGraphSameAs(base, Collections.singleton(sameAsPredicate), maxCacheSize);
    }

    public static DatasetGraph wrap(DatasetGraph base, Set<Node> sameAsPredicates, int maxCacheSize) {
        return new DatasetGraphSameAs(base, sameAsPredicates, maxCacheSize);
    }

    protected DatasetGraphSameAs(DatasetGraph base, Set<Node> sameAsPredicates, int maxCacheSize) {
        super(base);
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(maxCacheSize)
                .build(new CacheLoader<Entry<Node, Node>, Set<Triple>>() {
                    @Override
                    public Set<Triple> load(Entry<Node, Node> graphAndNode) throws Exception {
                        return loadDirectTriples(graphAndNode.getKey(), graphAndNode.getValue());
                    }
                });
        this.sameAsPredicates = sameAsPredicates;
    }

    @Override
    public Graph getDefaultGraph() {
        return GraphView.createDefaultGraph(this);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphView.createNamedGraph(this, graphNode);
    }

    @Override public void add(Node g, Node s, Node p, Node o) { add(new Quad(g, s, p, o)); }
    @Override public void delete(Node g, Node s, Node p, Node o) { delete(new Quad(g, s, p, o)); }

    @Override
    public void addAll(DatasetGraph src) {
        try {
            getLock().enterCriticalSection(Lock.WRITE);
            try (Stream<Quad> stream = src.stream()) {
                stream.forEach(quad -> performUpdateAction(quad, false, (ts, t) -> ts.add(t), () -> super.add(quad)));
            }
        } finally {
            getLock().leaveCriticalSection();
        }
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
//        if (NodeUtils.isNullOrAny(p) || sameAsPredicates.contains(p)) {
//        	if (NodeUtils.isNullOrAny(s)) {
//
//        	}
//
//        }
        // TODO For simplicity we just invalidate everything
        getLock().enterCriticalSection(Lock.WRITE);
        try {
            cache.invalidateAll();
            super.deleteAny(g, s, p, o);
        } finally {
            getLock().leaveCriticalSection();
        }
    }

    @Override
    public void add(Quad quad) {
        performUpdateAction(quad, true, (ts, t) -> ts.add(t), () -> super.add(quad));
    }

    @Override
    public void delete(Quad quad) {
        performUpdateAction(quad, true, (ts, t) -> ts.remove(t), () -> super.delete(quad));
    }

    public void performUpdateAction(Quad quad, boolean doLocking, BiConsumer<Set<Triple>, Triple> tripleAction, Runnable graphAction) {
        Set<Triple> ts;
        if (sameAsPredicates.contains(quad.getPredicate())) {
            if (doLocking) { getLock().enterCriticalSection(Lock.WRITE); }
            try {
                Triple t = quad.asTriple();
                ts = cache.getIfPresent(new SimpleEntry<>(quad.getGraph(), quad.getSubject()));
                if (ts != null) {
                    tripleAction.accept(ts, t);
                }

                ts = cache.getIfPresent(new SimpleEntry<>(quad.getGraph(), quad.getObject()));
                if (ts != null) {
                    tripleAction.accept(ts, t);
                }
                graphAction.run();
            } finally {
                if (doLocking) { getLock().leaveCriticalSection(); }
            }
        } else {
            graphAction.run();
        }
    }

    @Override
    public void abort() {
        // Note: Requesting a readLock if the write lock is already held should simply increment the lock count on the write lock
        getLock().enterCriticalSection(Lock.READ);
        try {
            cache.invalidateAll();
            super.abort();
        } finally {
            getLock().leaveCriticalSection();
        }
    }

    @Override
    protected Iterator<Quad> actionFind(Node gg, Node ss, Node pp, Node oo) {
        // If s or o is concrete then resolve their sameAs clusters before the lookup
        // Otherwise, resolve the sameAs clusters of that component based on the obtained triple's concrete values

        // gx = "given x"; mx = "match x" analogous to Triple.getMatchX()
        Node mg = NodeUtils.anyToNull(gg);
        Node ms = NodeUtils.anyToNull(ss);
        Node mp = NodeUtils.anyToNull(pp);
        Node mo = NodeUtils.anyToNull(oo);

        // Note: resolveSameAs only actually resolves the given start node if both it and the graph are concrete;
        // Otherwise the start node is returned as-is.

        // Set up the tuple stream (quads)
        Stream<Quad> ts =
            resolveSameAs(mg, ms).flatMap(s ->
                resolveSameAs(mg, mo).flatMap(o ->
                    getR().stream(mg, s, mp, o)));

        // ts = StreamUtils.println(ts);
        if (ms == null || mg == null) {
            // If the initial graph or subject were null then resolve sameAs based on the
            // concrete graph and subject components of the obtained quads
            ts = ts.flatMap(t -> resolveSameAs(t.getGraph(), t.getSubject()).map(s -> Quad.create(t.getGraph(), s, t.getPredicate(), t.getObject())));
        } else {
            // If the initial graph or subject were concrete then sameAs resolution already happened
            // However, replace any sameAs'd subject with the concrete one
            // (i.e. pretend all obtained triples belonged to the subject we used for the lookup)
            ts = ts.map(t -> t.getSubject().equals(ms) ? t : Quad.create(t.getGraph(), ms, t.getPredicate(), t.getObject()));
        }

        if (mo == null || mg == null) {
            ts = ts.flatMap(t -> resolveSameAs(t.getGraph(), t.getObject()).map(o -> Quad.create(t.getGraph(), t.getSubject(), t.getPredicate(), o)));
        } else {
            ts = ts.map(t -> t.getObject().equals(mo) ? t : Quad.create(t.getGraph(), t.getSubject(), t.getPredicate(), mo));
        }

        return Iter.onClose(ts.iterator(), ts::close);
    }

    protected Stream<Node> resolveSameAs(Node g, Node start) {
        Stream<Node> result;
        if (g == null || start == null) {
            result = Stream.of(start);
        } else {
            Traverser<Node> traverser = Traverser.forGraph(n -> getDirectNodes(g, n));
            // Note: Traverser always includes the start node in its result
            result = Streams.stream(traverser.breadthFirst(start));
        }
        // result = StreamUtils.viaList(result, list -> System.out.println("resolveSameAs: " + start + " -> " + list));
        return result;
    }

    public Set<Node> getDirectNodes(Node g, Node start) {
        Set<Node> triples = getDirectTriples(g, start).stream()
            .map(t -> t.getSubject().equals(start) ? t.getObject() : t.getSubject())
            .collect(Collectors.toSet());
        // System.out.println("Cluster for graph " + g + ": " + start + " -> " + triples);
        return triples;
    }

    /**
     * Get the set of all direct ingoing and outgoing triplesfor a given graph and node.
     * Lookup for literals immediately return an empty set - any other lookups go through the cache.
     *
     * @param g The graph for the starting node. Never null.
     * @param start The start node for which to compute the cluster. Never null.
     * @return
     */
    private Set<Triple> getDirectTriples(Node g, Node start) {
        Set<Triple> result;
        try {
             result = start.isLiteral()
                     ? Collections.emptySet()
                     : cache.get(new SimpleEntry<>(g, start));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /** This method should only be invoked by the cache callback */
    private Set<Triple> loadDirectTriples(Node g, Node s) {
        Set<Triple> result = streamDirectTriples(g, s).collect(Collectors.toSet());
        return result;
    }

    /** Stream direct triples in both directions */
    private Stream<Triple> streamDirectTriples(Node g, Node s) {
        return Stream.concat(
            sameAsPredicates.stream().flatMap(p -> streamDirectTriples(g, s, p, true)),
            sameAsPredicates.stream().flatMap(p -> streamDirectTriples(g, s, p, false)));
    }

    /** Stream direct triples in a specific direction (ingoing or outgoing) */
    private Stream<Triple> streamDirectTriples(Node g, Node s, Node p, boolean isForward) {
        Stream<Triple> result = isForward
                ? getR().stream(g, s, p, Node.ANY).map(Quad::asTriple)
                : getR().stream(g, Node.ANY, p, s).map(Quad::asTriple);
        return result;
    }

    public static void main(String[] args) {
        DatasetGraph base = DatasetGraphFactory.create();
        DatasetGraph datasetGraph = DatasetGraphSameAs.wrap(base);

        Dataset dataset = DatasetFactory.wrap(datasetGraph);
        Model model = dataset.getDefaultModel();

        Resource s = model.createResource("urn:example:s");
        Resource o = model.createResource("urn:example:o");

        s
            .addProperty(RDFS.label, "s")
            .addProperty(OWL.sameAs, o);

        o
            .addProperty(RDFS.label, "o")
            .addProperty(RDFS.label, "s");

        System.out.println("trig:");
        RDFDataMgr.write(System.out, dataset, RDFFormat.TRIG_PRETTY);

        System.out.println("find:");
        dataset.asDatasetGraph().find().forEachRemaining(System.out::println);

        System.out.println("s:");
        s.listProperties().forEachRemaining(System.out::println);

        System.out.println("o:");
        o.listProperties().forEachRemaining(System.out::println);

        System.out.println("labels:");
        model.listStatements(null, RDFS.label, (RDFNode)null).forEachRemaining(System.out::println);
    }
}

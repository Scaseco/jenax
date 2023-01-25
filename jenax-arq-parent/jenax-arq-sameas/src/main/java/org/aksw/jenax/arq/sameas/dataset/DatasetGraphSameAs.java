package org.aksw.jenax.arq.sameas.dataset;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
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
    extends DatasetGraphWrapper
    implements DatasetGraphWrapperView
{
    // private static final Logger logger = LoggerFactory.getLogger(GraphWithSameAsInference.class);

    protected LoadingCache<Entry<Node, Node>, Set<Node>> directClusters;
    protected Set<Node> sameAsPredicates;

    public static final int DFT_MAX_CACHE_SIZE = 1000;

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
        this.directClusters = CacheBuilder.newBuilder()
                .maximumSize(maxCacheSize)
                .build(new CacheLoader<Entry<Node, Node>, Set<Node>>() {
                    @Override
                    public Set<Node> load(Entry<Node, Node> graphAndNode) throws Exception {
                        return buildDirectCluster(graphAndNode.getKey(), graphAndNode.getValue());
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

    @Override
    public Iterator<Quad> find() {
        return find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
    }

    @Override
    public Iterator<Quad> find(Quad quad) {
        return coreFind(quad.getGraph(), quad.asTriple());
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        return coreFind(g, Triple.createMatch(s, p, o));
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        return find(g, s, p, o);
    }

    protected Iterator<Quad> coreFind(Node graph, Triple pattern) {
        // If s or o is concrete then resolve their sameAs clusters before the lookup
        // Otherwise, resolve the sameAs clusters of that component based on the obtained triple's concrete value

        Node g = NodeUtils.anyToNull(graph);
        Node mp = pattern.getMatchPredicate();
        Node ms = pattern.getMatchSubject();
        Node mo = pattern.getMatchObject();

        // System.out.println("Lookup: " + g + " - " + pattern);

        // Note: resolveSameAs only actually resolves the given start node if both it and the graph are concrete;
        // Otherwise the start node is returned as-is.

        // Set up the tuple stream (quads)
        Stream<Quad> ts =
            resolveSameAs(g, ms).flatMap(s ->
                resolveSameAs(g, mo).flatMap(o ->
                    getR().stream(g, s, mp, o)));

        // ts = StreamUtils.println(ts);

        if (ms == null || g == null) {
            // If the initial graph or subject were null then resolve sameAs based on the
            // concrete graph and subject components of the obtained quads
            ts = ts.flatMap(t -> resolveSameAs(t.getGraph(), t.getSubject()).map(s -> Quad.create(t.getGraph(), s, t.getPredicate(), t.getObject())));
        } else {
            // If the initial graph or subject were concrete then sameAs resolution already happened
            // However, replace any sameAs'd subject with the concrete one
            ts = ts.map(t -> t.getSubject().equals(ms) ? t : Quad.create(t.getGraph(), ms, t.getPredicate(), t.getObject()));
        }

        if (mo == null || g == null) {
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
            Traverser<Node> traverser = Traverser.forGraph(n -> getDirectCluster(g, n));
            // Note traverser always includes the start node in its result
            result = Streams.stream(traverser.breadthFirst(start));
        }

        // result = StreamUtils.viaList(result, list -> System.out.println("resolveSameAs: " + start + " -> " + list));

        return result;
    }

    /**
     *
     * @param g The graph for the starting node. Never null.
     * @param start The start node for which to compute the cluster. Never null.
     * @return
     */
    private Set<Node> getDirectCluster(Node g, Node start) {
        Set<Node> result;
        try {
             result = start.isLiteral()
                     ? Collections.singleton(start)
                    : directClusters.get(new SimpleEntry<>(g, start));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private Set<Node> buildDirectCluster(Node g, Node s) {
        Set<Node> result = streamDirectCluster(g, s).collect(Collectors.toSet());
        // System.out.println("Cluster for " + s + ": " + result);
        return result;
    }

    private Stream<Node> streamDirectCluster(Node g, Node s) {
        return Stream.concat(
            sameAsPredicates.stream().flatMap(p -> successors(g, s, p, true)),
            sameAsPredicates.stream().flatMap(p -> successors(g, s, p, false)));
    }

    private Stream<Node> successors(Node g, Node s, Node p, boolean isForward) {
        Stream<Node> result = isForward
                ? getR().stream(g, s, p, Node.ANY).map(Quad::getObject)
                : getR().stream(g, Node.ANY, p, s).map(Quad::getSubject);
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

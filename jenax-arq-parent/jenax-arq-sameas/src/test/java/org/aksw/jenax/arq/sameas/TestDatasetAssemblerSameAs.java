package org.aksw.jenax.arq.sameas;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.aksw.commons.util.exception.FinallyRunAll;
import org.aksw.jenax.arq.util.dataset.DatasetGraphRDFSReduced;
import org.aksw.jenax.arq.util.dataset.DatasetGraphSameAs;
import org.aksw.jenax.arq.util.dataset.DatasetGraphUnionDefaultGraph;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.ext.com.google.common.io.MoreFiles;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.assembler.VocabTDB2;
import org.apache.jena.vocabulary.OWL;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterators;

public class TestDatasetAssemblerSameAs {
    // Initialized/destroyed by beforeClass/afterClass methods
    // static DatasetGraph base;
    static Dataset ds1;
    static Path tdb2TmpFolder;

    @Test
    public void test01() {
        // runTest("SELECT * { ?s ?p ?o }", 8);
        // In union default graph  mode without deduplication we expect alot of triples
        // runTest("SELECT * { ?s ?p ?o } ORDER BY ?s ?p ?o", 27);

        // This test now assumes deduplication
        runTest("SELECT * { ?s ?p ?o } ORDER BY ?s ?p ?o", 18);
    }

    @Test
    public void test02() {
        runTest("SELECT * { GRAPH <urn:example:g1> { ?s ?p ?o } } ORDER BY ?s ?p ?o", 8);
    }

    @Test
    public void test03() {
        runTest("SELECT * { SERVICE <sameAs:> { ?s ?p ?o } } ORDER BY ?s ?p ?o", 18);
    }

    @Test
    public void sparql4() {
        Graph vocab = SSE.parseGraph("(graph (:p rdfs:range :R) (:p rdfs:domain :D) )");
        DatasetGraph dsg0 = DatasetGraphFactory.createTxnMem();

        DatasetGraph dsg = RDFSFactory.datasetRDFS(dsg0, vocab);
        dsg.executeWrite(() -> {
            dsg.add(SSE.parseQuad("(:g1 :x :p :y)"));
        });

        // dsg.find(null, null, null, null).forEachRemaining(System.out::println);
        // Named graphs, duplicates.
//        {
//            String qs2 = "SELECT * { GRAPH <" + Quad.unionGraph.getURI() + "> { ?s ?p ?o } }";
//            Query query2 = QueryFactory.create(qs2);
//
//            try (QueryExecution qExec = QueryExecutionFactory.create(query2, dsg)) {
//                ResultSet rs = qExec.execSelect();
//                ResultSetFormatter.out(rs);
//            }
//        }
    }


    @Test
    public void test04() {
        Graph baseGraph = SSE.parseGraph("(graph (:x rdfs:label 'x') (:y rdfs:label 'y') (:x owl:sameAs :y) (:a rdfs:label 'a') )");
        DatasetGraph baseDs = DatasetGraphFactory.wrap(baseGraph);
        DatasetGraph ds = DatasetGraphSameAs.wrapWithTable(baseDs, OWL.sameAs.asNode(), false);

        // ds.find().forEachRemaining(System.out::println);
    }

    // @Test
    public void experiment01() {
        experiment01Impl(ds1);

        Dataset ds2 = experiment01Impl(DatasetFactory.wrap(DatasetGraphRDFSReduced.wrap(
                DatasetGraphSameAs.wrap(
                DatasetGraphUnionDefaultGraph.wrap(
                DatasetGraphFactory.createTxnMem())),
                RDFDataMgr.loadGraph("/home/raven/Datasets/coypu/coy-ontology.ttl"))));

        boolean computeDiff = false;

        if (computeDiff) {
            Dataset ds1Mat = DatasetFactory.createTxnMem();
            Txn.executeWrite(ds1Mat, () -> Txn.executeRead(ds1,
                    () -> ds1Mat.asDatasetGraph().addAll(ds1.asDatasetGraph())));

            Dataset ds2Mat = DatasetFactory.createTxnMem();
            Txn.executeWrite(ds2Mat, () -> Txn.executeRead(ds2,
                    () -> ds2Mat.asDatasetGraph().addAll(ds2.asDatasetGraph())));


            Txn.executeWrite(ds2Mat, () -> {
                Txn.executeWrite(ds1Mat, () -> {
    //                dataset.asDatasetGraph().stream(Node.ANY, Node.ANY, NodeFactory.createURI("http://www.wikidata.org/prop/direct/P3271"), Node.ANY)
    //                    .forEach(System.out::println);

    //                ds.asDatasetGraph().stream(Node.ANY, Node.ANY, NodeFactory.createURI("http://www.wikidata.org/prop/direct/P3271"), Node.ANY)
    //                	.forEach(System.out::println);

                    try (Stream<Quad> quads = ds1Mat.asDatasetGraph().stream()) {
                        quads.forEach(ds2Mat.asDatasetGraph()::delete);
                    }
                    RDFDataMgr.write(System.out, ds2Mat, RDFFormat.TRIG_PRETTY);

    //                try (Stream<Quad> quads = ds2Mat.asDatasetGraph().stream()) {
    //                    quads.forEach(ds1Mat.asDatasetGraph()::delete);
    //                }
    //                RDFDataMgr.write(System.out, ds1Mat, RDFFormat.TRIG_PRETTY);
                });
            });
        }
    }

    public static  Dataset experiment01Impl(Dataset dataset) {
        Stopwatch sw = Stopwatch.createStarted();

        // DatasetGraph base = DatasetGraphFactory.create();
        System.out.println("Starting loading of data...");
        Txn.executeWrite(dataset, () -> {
            List<String> sources = Arrays.asList(
                    "/home/raven/Datasets/coypu/countries-deu.nt",
                    "/home/raven/Datasets/coypu/countries-deu-wikidata.nt"
                    // "/home/raven/Datasets/coypu/countries-deu-link.nt"
                    );
            DatasetGraph dsg = dataset.asDatasetGraph();
            long quadCount = 0;
            for (String source : sources) {
                // Node g = NodeFactory.createURI(source);
                Node g = NodeFactory.createURI("http://foo.bar/baz");
                try (Stream<Quad> stream = AsyncParser.of(source).streamQuads()) {
                    Iterator<Quad> it = stream
                            // .limit(1000)
                            .iterator();
                    while (it.hasNext()) {
                        Quad raw = it.next();
                        Quad quad = Quad.create(g, raw.asTriple());

//                        if (!quad.toString().contains("sameAs")) {
//                            continue;
//                        }

                        if (false && dsg.contains(quad)) {
                            System.out.println("Duplicate detected on insert: " + quad);
                        }

                        dsg.add(quad);
                        ++quadCount;
                    }
                }
            }
//          RDFDataMgr.read(dataset, "/home/raven/Datasets/coypu/countries-deu.nt");
//          RDFDataMgr.read(dataset, "/home/raven/Datasets/coypu/countries-deu-wikidata.nt");
//          RDFDataMgr.read(dataset, "/home/raven/Datasets/coypu/countries-deu-link.nt");
            System.out.println("Finished loading of " + quadCount + " quads in " + sw.elapsed(TimeUnit.SECONDS));
        });

        for (int x = 0; x < 1; ++x) {
            System.out.println("Starting retrieval...");
            sw.reset().start();
            DatasetGraph dsg = dataset.asDatasetGraph(); // DatasetGraphSameAs.wrap(base);
            Txn.executeRead(dsg,() -> {
                Iterator<Quad> it = dsg.find();
                // it.forEachRemaining(x -> System.out.println("Seen: " + x));
                int i = 0;
                Set<Quad> seenQuads = new HashSet<>();
                while (it.hasNext()) {
                    Quad quad = it.next();
                    // System.out.println(quad);
                    ++i;

                    if (seenQuads.contains(quad)) {
                        System.out.println("Duplicate detected on retrieval: " + quad);
                        seenQuads.add(quad);
                    }

                    if (quad.getObject().toString().toLowerCase().contains("osmelement")) {
                        System.out.println("Found: " + quad);
                    }

                    if (i % 1000000 == 0) {
                        System.out.println("Current count: " + i + " elapsed: " + sw.elapsed(TimeUnit.SECONDS));
                    }
                }
                System.out.println("Current count: " + i + " elapsed: " + sw.elapsed(TimeUnit.SECONDS));
                Iter.close(it);
            });
        }
        System.out.println("Finished action in " + sw.elapsed(TimeUnit.SECONDS));
        return dataset;
    }

    public void runTest(String queryStr, int expectedResult) {
        Query query = QueryFactory.create(queryStr);
        int actualResult = exec(query);
        Assert.assertEquals(expectedResult, actualResult);
    }

    public int exec(Query query) {
        int result = Txn.calculateRead(ds1, () -> {
            try (QueryExecution qe = QueryExecution.create(query, ds1)) {
                int r;
                if (query.isSelectType()) {
                    ResultSetRewindable rs = ResultSetFactory.makeRewindable(qe.execSelect());
                    // ResultSetFormatter.outputAsTSV(System.out, rs); rs.reset();
                    r = ResultSetFormatter.consume(rs);
                } else if (query.isConstructType()) {
                    r = Iterators.size(qe.execConstructQuads());
                } else {
                    throw new RuntimeException("Unsupported query type");
                }
                return r;
            }
        });
        return result;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        tdb2TmpFolder = Files.createTempDirectory("jenax_sameas_tdb2").toAbsolutePath();

        String assemblerStr = String.join("\n",
                "PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>",
                "PREFIX tdb2: <http://jena.apache.org/2016/tdb#>",
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>",
                "PREFIX jxp: <http://jenax.aksw.org/plugin#>",
                // "<urn:example:root> a ja:DatasetRDFS ; ja:rdfsSchema \"/home/raven/Datasets/coypu/coy-ontology.ttl\" ; ja:dataset <urn:example:rootu> .",
                // "<urn:example:root> a jxp:DatasetRDFS ; ja:rdfsSchema '/home/raven/Datasets/coypu/coy-ontology.ttl' ; ja:dataset <urn:example:rootu> .",
                "<urn:example:root> a jxp:DatasetSameAs ; jxp:allowDuplicates false ; jxp:cacheMaxSize -1 ; jxp:predicate owl:sameAs ; ja:baseDataset [ a jxp:DatasetAutoUnionDefaultGraph ; ja:baseDataset <urn:example:base> ] .",
                "<urn:example:base> a tdb2:DatasetTDB2 ; tdb2:unionDefaultGraph true ."
                // "<urn:example:base> a ja:MemoryDataset ."
            );

        String dataStr = String.join("\n",
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>",
                "<urn:example:s> rdfs:label 's' ; owl:sameAs <urn:example:o> . <urn:example:o> rdfs:label 'o' .",
                "<urn:example:g1> { <urn:example:x> rdfs:label 'x' . <urn:example:o> rdfs:label 'o' ; owl:sameAs <urn:example:x> . }",
                "<urn:example:g2> { <urn:example:y> rdfs:label 'y' ; owl:sameAs <urn:example:o> . }"
            );

        String effDataStr = dataStr;

        Model confModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(confModel, new StringReader(assemblerStr), null, Lang.TURTLE);
        Resource tdb2Conf = confModel.getResource("urn:example:base");
        tdb2Conf.addProperty(VocabTDB2.pLocation, tdb2TmpFolder.toString());

        ds1 = DatasetFactory.assemble(confModel.getResource("urn:example:root"));
        Txn.executeWrite(ds1, () -> RDFDataMgr.read(ds1, new StringReader(effDataStr), null, Lang.TRIG));

        // base = dataset.asDatasetGraph();
        // base = ((DatasetGraphWrapper)base).getWrapped();
        // base = ((DatasetGraphWrapper)base).getWrapped();
        // Txn.executeRead(dataset, () -> RDFDataMgr.write(System.out, dataset, RDFFormat.TRIG_PRETTY));
    }

    @AfterClass
    public static void afterClass() {
        FinallyRunAll.run(
                () -> Optional.ofNullable(ds1).ifPresent(Dataset::close),
                () -> { if (tdb2TmpFolder != null) { MoreFiles.deleteRecursively(tdb2TmpFolder); } });
    }
}

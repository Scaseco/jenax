package org.aksw.jenax.arq.sameas;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.util.exception.FinallyRunAll;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.ext.com.google.common.io.MoreFiles;
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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.assembler.VocabTDB2;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterators;

public class TestDatasetAssemblerSameAs {
    // Initialized/destroyed by beforeClass/afterClass methods
    // static DatasetGraph base;
    static Dataset dataset;
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


    // @Test
    public void experiment01() {
        Stopwatch sw = Stopwatch.createStarted();

        // DatasetGraph base = DatasetGraphFactory.create();
        Txn.executeWrite(dataset, () -> {
            RDFDataMgr.read(dataset, "/home/raven/Datasets/coypu/countries-deu.nt");
            RDFDataMgr.read(dataset, "/home/raven/Datasets/coypu/countries-deu-wikidata.nt");
        });
        System.out.println("Finished loading in " + sw.elapsed(TimeUnit.SECONDS));
        sw.reset().start();
        DatasetGraph dsg = dataset.asDatasetGraph(); // DatasetGraphSameAs.wrap(base);
        Txn.executeRead(dsg,() -> {
            Iterator<Quad> it = dsg.find();
            //it.forEachRemaining(x -> System.out.println("Seen: " + x));
            int i = 0;
            while (it.hasNext()) {
                it.next();
                ++i;

                if (i % 100000 == 0) {
                    System.out.println("Current count: " + i + " elapsed: " + sw.elapsed(TimeUnit.SECONDS));
                }
            }
            System.out.println("Current count: " + i + " elapsed: " + sw.elapsed(TimeUnit.SECONDS));
            Iter.close(it);
        });
        System.out.println("Finished action in " + sw.elapsed(TimeUnit.SECONDS));
    }

    public void runTest(String queryStr, int expectedResult) {
        Query query = QueryFactory.create(queryStr);
        int actualResult = exec(query);
        Assert.assertEquals(expectedResult, actualResult);
    }

    public int exec(Query query) {
        int result = Txn.calculateRead(dataset, () -> {
            try (QueryExecution qe = QueryExecution.create(query, dataset)) {
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
                "<urn:example:root> a jxp:DatasetSameAs ; jxp:cacheMaxSize 1000 ; jxp:predicate owl:sameAs ; ja:baseDataset [ a jxp:DatasetAutoUnionDefaultGraph ; ja:baseDataset <urn:example:base> ] .",
                "<urn:example:base> a tdb2:DatasetTDB2 ; tdb2:unionDefaultGraph true ."
                // "<urn:example:base> a ja:MemoryDataset ."
            );

        String dataStr = String.join("\n",
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>",
                "<urn:example:s> rdfs:label \"s\" ; owl:sameAs <urn:example:o> . <urn:example:o> rdfs:label \"o\" .",
                "<urn:example:g1> { <urn:example:x> rdfs:label \"x\" . <urn:example:o> rdfs:label \"o\" ; owl:sameAs <urn:example:x> . }",
                "<urn:example:g2> { <urn:example:y> rdfs:label \"y\" ; owl:sameAs <urn:example:o> . }"
            );

        Model confModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(confModel, new StringReader(assemblerStr), null, Lang.TURTLE);
        Resource tdb2Conf = confModel.getResource("urn:example:base");
        tdb2Conf.addProperty(VocabTDB2.pLocation, tdb2TmpFolder.toString());

        dataset = DatasetFactory.assemble(confModel.getResource("urn:example:root"));
        Txn.executeWrite(dataset, () -> RDFDataMgr.read(dataset, new StringReader(dataStr), null, Lang.TRIG));

        // base = dataset.asDatasetGraph();
        // base = ((DatasetGraphWrapper)base).getWrapped();
        // base = ((DatasetGraphWrapper)base).getWrapped();
        // Txn.executeRead(dataset, () -> RDFDataMgr.write(System.out, dataset, RDFFormat.TRIG_PRETTY));
    }

    @AfterClass
    public static void afterClass() {
        FinallyRunAll.run(
                () -> Optional.ofNullable(dataset).ifPresent(Dataset::close),
                () -> { if (tdb2TmpFolder != null) { MoreFiles.deleteRecursively(tdb2TmpFolder); } });
    }
}

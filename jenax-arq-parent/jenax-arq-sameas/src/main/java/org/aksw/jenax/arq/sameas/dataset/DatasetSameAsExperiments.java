package org.aksw.jenax.arq.sameas.dataset;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.base.Stopwatch;

/** This class is subject to deletion */
public class DatasetSameAsExperiments {

    /** Experiment to see the how many items are being buffered when turning a stream to an iterator */
    public static void main3(String[] args) {
        List<Integer> values = IntStream.range(0, 1000).boxed().collect(Collectors.toList());

        Stream<Entry<String, Integer>> stream;

        if (false) {
            stream =
                values.stream().flatMap(x ->
                    values.stream().flatMap(y ->
                        IntStream.range(0, 10).boxed().map(z -> Map.entry(x + "x" + y, z))
                            .peek(e -> System.out.println(Thread.currentThread() + " Got value"))
                ));
        } else {
            stream =
                values.stream().flatMap(x ->
                    values.stream().map(y -> Map.entry(x, y)))
                .flatMap(e ->
                        IntStream.range(0, 10).boxed().map(z -> Map.entry(e.getKey() + "x" + e.getValue(), z)))
                .peek(e -> System.out.println(Thread.currentThread() + " Got value"));
        }

        Iterator<?> it = stream.iterator();
        while (it.hasNext()) {
            System.err.println(Thread.currentThread() + " Result Item: " + it.next());
        }

    }

    public static void main(String[] args) {
        Stopwatch sw = Stopwatch.createStarted();

        DatasetGraph base = DatasetGraphFactory.createTxnMem();
        Txn.executeWrite(base, () -> {
            RDFDataMgr.read(base, "/home/raven/Datasets/coypu/countries-deu.nt");
            RDFDataMgr.read(base, "/home/raven/Datasets/coypu/countries-deu-wikidata.nt");
        });
        System.out.println("Finished loading in " + sw.elapsed(TimeUnit.SECONDS));
        sw.reset().start();
        DatasetGraph dsg = DatasetGraphSameAs.wrap(base);
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

    public static void main2(String[] args) {
        DatasetGraph base = DatasetGraphFactory.createTxnMem();
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

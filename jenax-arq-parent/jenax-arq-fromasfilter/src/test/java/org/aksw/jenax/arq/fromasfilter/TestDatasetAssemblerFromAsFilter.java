package org.aksw.jenax.arq.fromasfilter;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.aksw.commons.util.exception.FinallyRunAll;
import com.google.common.io.MoreFiles;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.assembler.VocabTDB2;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Iterators;

public class TestDatasetAssemblerFromAsFilter {

    // Initialized/destroyed by beforeClass/afterClass methods
    static Path tdb2TmpFolder;
    static Dataset dataset;

    @Test
    public void test01() {
        runTest("SELECT * FROM <urn:example:g1> FROM <urn:example:g2> { ?s ?p ?o }", 4);
    }

    @Test
    public void test02() {
        runTest("CONSTRUCT { GRAPH ?g { ?s ?p ?o } } WHERE { { SELECT * { GRAPH ?g { ?s ?p ?o } } LIMIT 10 } }", 4);
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
                    ResultSet rs = qe.execSelect();
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
        tdb2TmpFolder = Files.createTempDirectory("jena_from-enhancer_tdb2").toAbsolutePath();
        String assemblerStr = String.join("\n",
                "PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>",
                "PREFIX fe: <http://jena.apache.org/from-enhancer#>",
                "PREFIX xdt: <http://jsa.aksw.org/dt/sparql/>",
                "PREFIX tdb2: <http://jena.apache.org/2016/tdb#>",
                "<urn:example:root> a fe:DatasetFromAsFilter ; ja:baseDataset <urn:example:base> .",
                "<urn:example:root> fe:alias [ fe:graph <urn:example:all> ; fe:expr 'true'^^xdt:expr] .",
                "<urn:example:base> a tdb2:DatasetTDB2 ."
                // "<urn:example:base> a ja:MemoryDataset ."
            );

        String dataStr = String.join("\n",
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                "<urn:example:g1> { <urn:example:s> a <urn:example:class> ; rdfs:label \"s\" }",
                "<urn:example:g2> { <urn:example:s> a <urn:example:class> ; rdfs:label \"s\" }");

        Model confModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(confModel, new StringReader(assemblerStr), null, Lang.TURTLE);
        Resource tdb2Conf = confModel.getResource("urn:example:base");
        tdb2Conf.addProperty(VocabTDB2.pLocation, tdb2TmpFolder.toString());

        dataset = DatasetFactory.assemble(confModel.getResource("urn:example:root"));
        Txn.executeWrite(dataset, () -> RDFDataMgr.read(dataset, new StringReader(dataStr), null, Lang.TRIG));
    }

    @AfterClass
    public static void afterClass() {
        FinallyRunAll.run(
                () -> Optional.ofNullable(dataset).ifPresent(Dataset::close),
                () -> { if (tdb2TmpFolder != null) { MoreFiles.deleteRecursively(tdb2TmpFolder); } });
    }
}

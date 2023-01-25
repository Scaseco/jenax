package org.aksw.jenax.arq.sameas;

import java.io.StringReader;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.system.Txn;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Iterators;

public class TestDatasetAssemblerSameAs {

    // Initialized/destroyed by beforeClass/afterClass methods
    static Dataset dataset;

    @Test
    public void test01() {
        runTest("SELECT * { ?s ?p ?o }", 8);
    }

    @Test
    public void test02() {
        runTest("SELECT * { GRAPH ?g { ?s ?p ?o } }", 8);
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
                    // ResultSetFormatter.outputAsTSV(System.out, rs);
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
        String assemblerStr = String.join("\n",
                "PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>",
                "PREFIX sa: <http://jenax.aksw.org/plugin/sameas#>",
                "PREFIX tdb2: <http://jena.apache.org/2016/tdb#>",
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>",
                "<urn:example:root> a sa:DatasetSameAs ; sa:cacheMaxSize 1000 ; sa:predicate owl:sameAs ; ja:baseDataset <urn:example:base> .",
                "<urn:example:base> a ja:MemoryDataset ."
            );

        String dataStr = String.join("\n",
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>",
                "<urn:example:s> rdfs:label \"s\" ; owl:sameAs <urn:example:o> . <urn:example:o> rdfs:label \"o\" .",
                "<urn:example:g> { <urn:example:x> rdfs:label \"x\" . <urn:example:o> rdfs:label \"o\" ; owl:sameAs <urn:example:x> . }"
            );

        Model confModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(confModel, new StringReader(assemblerStr), null, Lang.TURTLE);
        dataset = DatasetFactory.assemble(confModel.getResource("urn:example:root"));
        Txn.executeWrite(dataset, () -> RDFDataMgr.read(dataset, new StringReader(dataStr), null, Lang.TRIG));
    }

    @AfterClass
    public static void afterClass() {
    }
}

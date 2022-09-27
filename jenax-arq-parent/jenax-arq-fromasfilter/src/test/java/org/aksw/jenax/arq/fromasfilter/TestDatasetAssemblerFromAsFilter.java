package org.aksw.jenax.arq.fromasfilter;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.ext.com.google.common.io.MoreFiles;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.assembler.VocabTDB2;
import org.junit.Assert;
import org.junit.Test;

public class TestDatasetAssemblerFromAsFilter {

    @Test
    public void testAssemblerFromAsFilter01() throws IOException {
        Path tdb2TmpFolder = Files.createTempDirectory("jena-service-enhancer-tdb2").toAbsolutePath();
        try {
            String assemblerStr = String.join("\n",
                    "PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>",
                    "PREFIX faf: <http://jena.apache.org/from-as-filter#>",
                    "PREFIX tdb2: <http://jena.apache.org/2016/tdb#>",
                    "<urn:example:root> a faf:DatasetFromAsFilter ; ja:baseDataset <urn:example:base> .",
                    "<urn:example:base> a tdb2:DatasetTDB2 ."
                );

            String dataStr = String.join("\n",
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                    "<urn:example:g1> { <urn:example:s> a <urn:example:class> ; rdfs:label \"s\" }",
                    "<urn:example:g2> { <urn:example:s> a <urn:example:class> ; rdfs:label \"s\" }");

            String queryStr = "SELECT * FROM <urn:example:g1> FROM <urn:example:g2> { ?s ?p ?o }";

            Model confModel = ModelFactory.createDefaultModel();
            RDFDataMgr.read(confModel, new StringReader(assemblerStr), null, Lang.TURTLE);
            Resource tdb2Conf = confModel.getResource("urn:example:base");
            tdb2Conf.addProperty(VocabTDB2.pLocation, tdb2TmpFolder.toString());

            Dataset dataset = DatasetFactory.assemble(confModel.getResource("urn:example:root"));
            try {
                Txn.executeWrite(dataset, () -> RDFDataMgr.read(dataset, new StringReader(dataStr), null, Lang.TRIG));

                int actualRowCount = Txn.calculateRead(dataset, () -> {
                    try (QueryExecution qe = QueryExecution.create(queryStr, dataset)) {
                        ResultSet rs = qe.execSelect();
                        return ResultSetFormatter.consume(rs);
                    }
                });

                // From-as-filter's (faf) non-standard semantics leads to 4 bindings because it does not compute an
                // RDF merge and/or apply distinct
                // Without faf this test case would return 2 bindings.
                Assert.assertEquals(4, actualRowCount);
            } finally {
                dataset.close();
            }

        } finally {
            MoreFiles.deleteRecursively(tdb2TmpFolder);
        }
    }

//    @Test
//    public void testFromClause() {
//        Model spec = ModelFactory.createDefaultModel();
//        RDFDataMgr.read(spec, new StringReader(SPEC_STR_01), null, Lang.TURTLE);
//        Dataset dataset = DatasetFactory.assemble(spec.getResource("urn:example:root"));
//
//        try (QueryExecution qe = QueryExecutionFactory.create("SELECT * FROM <urn:example:first> FROM <urn:example:second> { ?s ?p ?o }", dataset)) {
//            ResultSetFormatter.consume(qe.execSelect());
//        }
//    }

}

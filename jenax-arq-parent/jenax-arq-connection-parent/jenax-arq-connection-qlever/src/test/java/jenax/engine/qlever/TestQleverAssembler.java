package jenax.engine.qlever;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSetOps;
import org.junit.Test;

import com.google.common.io.MoreFiles;

public class TestQleverAssembler {

    @Test
    public void testQleverAssembler() throws IOException {
        Path tmpFolder = Files.createTempDirectory("qlever-assembler-test-");
        try {
            String str = """
                PREFIX rdf:       <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX rdfs:      <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX owl:       <http://www.w3.org/2002/07/owl#>
                PREFIX ja:        <http://jena.hpl.hp.com/2005/11/Assembler#>
                PREFIX qlever:    <http://jena.apache.org/qlever#>

                <urn:base> a qlever:Dataset ;
                    qlever:location "LOCATION" ;
                    qlever:indexName "myindex" ;
                    qlever:accessToken "abc" ;
                    .
                """
                    .replace("LOCATION", tmpFolder.toString());

            Resource conf = RDFParserBuilder.create()
                .lang(Lang.TURTLE)
                .fromString(str)
                .toModel()
                .createResource("urn:base");

            Dataset dataset = DatasetFactory.assemble(conf);
            try {
                Table table = QueryExec.dataset(dataset.asDatasetGraph()).query("SELECT (COUNT(*) AS ?c) { ?s a ?t . ?s ?p ?o }").table();
                RowSetOps.out(table.toRowSet());
            } finally {
                dataset.close();
            }
        } finally {
            MoreFiles.deleteRecursively(tmpFolder);
        }
    }
}

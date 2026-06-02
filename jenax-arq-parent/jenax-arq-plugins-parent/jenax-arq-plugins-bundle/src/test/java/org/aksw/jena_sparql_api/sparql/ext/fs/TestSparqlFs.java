package org.aksw.jena_sparql_api.sparql.ext.fs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import org.aksw.jenax.arq.util.security.ArqSecurity;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;

public class TestSparqlFs {

    public static Table exec(String queryStr) {
        DatasetGraph emptyDsg = DatasetGraphFactory.empty();
        Table result =  QueryExec.dataset(emptyDsg).query(queryStr)
                .set(ArqSecurity.symAllowFileAccess, true).table();
        return result;
    }

    protected URI getTestResourceUri(String resourceName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (url == null) {
            throw new IllegalStateException("Test resource not found: " + resourceName);
        }
        try {
            // Convert to URI first to handle spaces and special characters safely
            URI uri = url.toURI();

            // If it is a file schema and missing the triple slash, reconstruct it
            if ("file".equalsIgnoreCase(uri.getScheme()) && !uri.toString().startsWith("file:///")) {
                return new URI("file", "", uri.getPath(), uri.getFragment());
            }

            return uri;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSelectFrom() {
        String resourceUri = getTestResourceUri("fs/bikesharing-stations.dcat.ttl").toString();
        String queryStr = """
            SELECT * {
              SERVICE <$SERVICE> {
                ?s ?p ?o
              }
            }
            """.replace("$SERVICE", resourceUri);

        Table actualTable = exec(queryStr);
        int count = actualTable.size();
        assertEquals(21, count, "SELECT * FROM should return all triples from the DCAT file");
    }

    @Test
    public void testServiceLocalDataset() {
        String resourceUri = getTestResourceUri("fs/bikesharing-stations.dcat.ttl").toString();
        String queryStr = """
            SELECT * {
              VALUES (?s) { (<http://qrowd-project.eu/resource/electric-bikesharing-stations>) }
              SERVICE <$SERVICE> {
                ?s ?p ?o
              }
            }
            """.replace("$SERVICE", resourceUri);

        Table actualTable = exec(queryStr);
        int count = actualTable.size();
        assertEquals(8, count, "SERVICE query should return 8 triples for the electric-bikesharing-stations subject");
    }

    @Test
    public void testServiceWithVariableBinding() {
        String resourceUri = getTestResourceUri("fs/bikesharing-stations.dcat.ttl").toString();
        String queryStr = """
            SELECT * {
              VALUES (?x) { ( <$SERVICE> ) }
              VALUES (?s) { ( <http://qrowd-project.eu/resource/electric-bikesharing-stations>) }
              SERVICE ?x {
                ?s ?p ?o
              }
            }
            """.replace("$SERVICE", resourceUri);

        Table actualTable = exec(queryStr);
        int count = actualTable.size();
        assertEquals(8, count, "SERVICE with variable binding should return 8 triples for the station subject");
    }

    @Test
    public void testFsFindRdfFiles() {
        URI resourceUri = getTestResourceUri("fs/bikesharing-stations.dcat.ttl");
        String parentDirUri = resourceUri.resolve("../").toString();

        String queryStr = """
            PREFIX norse: <https://w3id.org/aksw/norse#>
            SELECT * {
              <$PARENT> norse:fs.find ?file
              BIND(norse:fs.probeRdf(?file) AS ?isRdf)
              BIND(norse:fs.rdfLang(?file) AS ?lang)
            }
            """.replace("$PARENT", parentDirUri);

        Table actualTable = exec(queryStr);
        int count = actualTable.size();
        assertTrue(count > 0, "fs:find should discover at least one RDF file in the test resource directory");
    }

    @Test
    public void testFsFindRdfLang() {
        String parentDirUri = getTestResourceUri("fs").toString();
        String queryStr = """
            PREFIX norse: <https://w3id.org/aksw/norse#>
            SELECT ?file ?lang {
              <$PARENT> norse:fs.find ?file
              BIND(norse:fs.rdfLang(?file) AS ?lang)
              FILTER(BOUND(?lang))
            }
            """.replace("$PARENT", parentDirUri);

        Table actualTable = exec(queryStr);
        int count = actualTable.size();
        assertEquals(1, count, "fs:rdfLang should identify exactly one RDF language for the DCAT file");
    }
}

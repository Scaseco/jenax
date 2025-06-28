package jenax.engine.qlever;

import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabase;
import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabaseFactory;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngines;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineDecorator;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactoryRegistry;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransforms;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.algebra.Table;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Closer;

public class TestQleverSystem {
    @Test
    public void test() throws Exception {
        RDFEngineFactoryRegistry registry = RDFEngineFactoryRegistry.get();

        // String systemName = "qlever:commit-f59763c";
        String systemName = "qlever";

        Closer closer = Closer.create();
        try {
            // Set up a temp directory for the database
            // and a temp file with test data.
            Path tmpDbFolder = Files.createTempDirectory("qlever-dbs");
            closer.register(() -> Files.delete(tmpDbFolder));

            Path ntFile = Files.createTempFile("qlever-test-", ".nt");
            closer.register(() -> Files.delete(ntFile));
            Files.writeString(ntFile, "<urn:s> <urn:p> <urn:o> .\n");

            Path rdfXmlFile = Files.createTempFile("qlever-test-", ".rdf");
            closer.register(() -> Files.delete(rdfXmlFile));
            Files.writeString(rdfXmlFile, """
                <?xml version="1.0"?>
                <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                            xmlns:dc="http://purl.org/dc/elements/1.1/"
                            xmlns:ex="http://example.org/stuff/1.0/">

                  <rdf:Description rdf:about="http://www.w3.org/TR/rdf-syntax-grammar"
                             dc:title="RDF1.1 XML Syntax">
                    <ex:editor>
                      <rdf:Description ex:fullName="Dave Beckett">
                        <ex:homePage rdf:resource="http://purl.org/net/dajobe/" />
                      </rdf:Description>
                    </ex:editor>
                  </rdf:Description>

                </rdf:RDF>
                """);



            // Get the database loader and create a database.
            RDFDatabaseFactory loader = registry.getDatabaseFactory(systemName);
            RDFDatabase database = loader.newBuilder()
                .setOutputFolder(tmpDbFolder)
                .addPath(ntFile.toString())
                .addPath(rdfXmlFile.toString())
                .build();

            // Make sure to delete the database's file set on exit.
            closer.register(() -> database.getFileSet().delete());

            // Get the engine factory and create an instance with the
            // pre-created database. An access token is needed for updates.
            // XXX Perhaps generate a random token if none specified to make updates work out of the box?
            RDFEngineFactory engineFactory = registry.getEngineFactory(systemName);
            try (RDFEngine engine = engineFactory.newEngineBuilder()
                .setDatabase(database)
                .setProperty("accessToken", "abcde")
                .setAutoDeleteIfCreated(true)
                .build()) {

                // Add two tripes.
                try (RDFLink link = engine.getLinkSource().newLink()) {
                    link.update("INSERT DATA { <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .}");
                    link.update("INSERT DATA { <urn:s> <urn:p> <urn:o> .}");
                }

                // Decorate the raw engine. The decorated RDF engine inherits the resource
                // management of the base engine.
                RDFEngineDecorator<?> engineDecorator = RDFEngines.decorate(engine);
                engineDecorator.decorate(RDFLinkTransforms.withLimit(1));

                try (RDFEngine decoratedEngine = engineDecorator.build()) {

                    decoratedEngine.getServiceControl().get().stop();

                    RDFDataSource dataSource = decoratedEngine.getLinkSource().asDataSource();

                    decoratedEngine.getServiceControl().get().start();

                    // The "limit 1" decorator is expected to work.
                    Table table = dataSource.asLinkSource().newQuery().query("SELECT * { ?s ?p ?o }").table();
                    Assert.assertEquals(table.size(), 1);
                }
            }
        } catch (Throwable t) {
            throw closer.rethrow(t);
        } finally {
            closer.close();
        }
    }
}

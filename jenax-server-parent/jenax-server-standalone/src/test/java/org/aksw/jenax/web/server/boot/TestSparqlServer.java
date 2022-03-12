package org.aksw.jenax.web.server.boot;

import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.web.server.boot.FactoryBeanSparqlServer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.junit.Assert;
import org.junit.Test;

public class TestSparqlServer {


    @Test
    public void testSparqlServer() throws Exception {

        Dataset dataset = DatasetFactory.create();
        RdfDataSource dataSource = () -> RDFConnectionFactory.connect(dataset);

        int port = 7529;
        Server server = new FactoryBeanSparqlServer()
                .setSparqlServiceFactory(dataSource)
                .setPort(port)
                .create();

        while (server.isStarting()) {
            Thread.sleep(1000);
        }

        RDFConnection conn = RDFConnectionFactory.connect("http://localhost:" + port + "/sparql");

        conn.update("INSERT DATA { <urn:x> <urn:x> <urn:x> }");

        Model actualModel = conn.queryConstruct(QueryFactory.create("CONSTRUCT WHERE { ?s ?p ?o }"));

        Model expectedModel = ModelFactory.createDefaultModel();
        Node x = NodeFactory.createURI("urn:x");
        expectedModel.getGraph().add(x, x, x);

        Assert.assertTrue(expectedModel.isIsomorphicWith(actualModel));

        // FIXME Create a separate test as Jena 3.9.0 fails with this query because it picks the wrong content type
//		Model model = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");

        server.stop();
        server.join();

    }
}

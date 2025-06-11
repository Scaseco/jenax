package org.aksw.jenax.graphql.sparql;

import org.aksw.jenax.fuseki.mod.graphql.ServerUtils;
import org.aksw.jenax.web.servlet.graphql.GraphQlUi;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestGraphQLFuseki {
    private DatasetGraph dsg;
    private FusekiServer server;

    @Before
    public void setUp() {
        dsg = DatasetGraphFactory.create();
        setupTestData(dsg);

        String graphQlUiResource = ServerUtils.getExternalForm(GraphQlUi.class);
        // System.out.println(getExternalForm(GraphQlUi.class));

//        if (true) {
//            throw new RuntimeException("Aborted.");
//        }
//        // Variante 1: Die Files via mehrerer servlets einklinken (jedes file ein servlet)
//        // Variante 2:
//        HttpServlet serv = null;

        System.setProperty("FUSEKI_BASE", "/home/raven/Repositories/coypu/fuseki-with-jenax/run/configuration");

        String[] argv = new String[] { "--empty" };
        server = FusekiMain.builder(argv)
            .parseConfigFile("/home/raven/Repositories/coypu/fuseki-with-jenax/run/config.ttl")
            // .addServlet("graphql.bundle.js", serv)
            // .staticFileBase("/home/raven/Projects/Eclipse/jenax/jenax-graphql-parent/jenax-graphql-v2-parent/jenax-graphql-v2-ui/frontend/build")
            // .staticFileBase(Path.of("").toAbsolutePath().toString())
            .add("test", dsg)
            .build();
        server.start();
        int port = server.getPort();
        String serverURL = "http://localhost:" + port + "/";
        String graphqlUrl = serverURL + "test/graphql";
        System.out.println(graphqlUrl);
    }

    private void setupTestData(DatasetGraph dsg) {
        dsg.getDefaultGraph().add(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.type);
//        // Fill the graph with a few geometries; spatial index construction will derive the SRS from them.
//        Envelope envelope = new Envelope(-175, 175, -85, 85);
//
//        Map<GeometryType, Number> conf = new HashMap<>();
//        conf.put(GeometryType.POINT, 1);
//
//        // Generate geometries into the default graph and a named graph
//        GeometryGenerator.generateGraph(dsg.getDefaultGraph(), envelope, conf);
//
//        conf.put(GeometryType.POLYGON, 1);
//        GeometryGenerator.generateGraph(dsg.getGraph(graphName1), envelope, conf);
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void test() throws InterruptedException {
         // Thread.sleep(100000);
    }
}

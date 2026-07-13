package org.aksw.jenax.dataaccess.sparql.dataset.rdflink.assembler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.graph.Graph;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpRDF;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;

public class TestDatasetAssemblerHTTP {

    @Test
    public void test() {
//        FusekiServer server = FusekiServer.create().port(3456).build();
//
//        ServletContext cxt = server.getServletContext();
//        FusekiServer server2 = FusekiServer.get(cxt);
//        assertNotNull(server2);
//        assertEquals(server, server2);
        DatasetGraph backendDsg = DatasetGraphFactory.createTxnMem();
        Txn.executeWrite(backendDsg,  ()->{
            Quad q = SSE.parseQuad("(_ :s :p _:b)");
            backendDsg.add(q);
        });

        DataService dataService = DataService.newBuilder(backendDsg)
            // .addEndpoint(Operation.GSP_RW)
            .addEndpoint(Operation.Query)
            // .addEndpoint(Operation.Update)
            .build();
        FusekiServer server = FusekiServer.create()
            .port(0)
            .add("/data", dataService)
            .build();
        server.start();
        int port = server.getPort();

        String queryEndpointUrl = "http://localhost:" + port;

        String assemblerStr= """
            PREFIX jds: <https://w3id.org/aksw/jena/dataset#>

            <urn:root>
              a jds:DatasetHTTP ;
              jds:auth [
              #  jds:user "user" ;
              #  jds:pass "pass" ;
              ] ;
              jds:destination <ENDPOINT_URL> ;
              .
        """.replace("ENDPOINT_URL", queryEndpointUrl);

        Resource assemblerRes = RDFParser.fromString(assemblerStr, Lang.TURTLE).toModel().getResource("urn:root");

        Dataset frontendDs = DatasetFactory.assemble(assemblerRes);
        DatasetGraph frontendDsg = frontendDs.asDatasetGraph();

        try {
            // Put data in.
            String data = "(graph (:s :p 1) (:s :p 2) (:s :p 3))";
            Graph g = SSE.parseGraph(data);

            // POST (This is posint to the GSP_RW service with no name -> quads operation.
//            HttpRDF.httpPutGraph(HttpEnv.getDftHttpClient(), destination, g, RDFFormat.NT);
            // GET
//            Graph g2 = HttpRDF.httpGetGraph(destination);
//            assertTrue(g.isIsomorphicWith(g2));

            Table table1 = QueryExec.dataset(frontendDsg).query("SELECT * { ?s ?p ?o }").table();

            System.out.println(table1);

//            // Query.
//            query("http://localhost:"+port+"/data", "SELECT * { ?s ?p ?o}", qExec->{
//                RowSet rs = qExec.select();
//                long x = Iter.count(rs);
//                assertEquals(3, x);
//            });
//            // Update
//            UpdateExecution.service("http://localhost:"+port+"/data").update("CLEAR DEFAULT").execute();
//            // Query again.
//            query("http://localhost:"+port+"/data", "SELECT * { ?s ?p ?o}", qExec-> {
//                RowSet rs = qExec.select();
//                assertFalse(rs.hasNext());
//            });
        } finally { server.stop(); }
    }
}

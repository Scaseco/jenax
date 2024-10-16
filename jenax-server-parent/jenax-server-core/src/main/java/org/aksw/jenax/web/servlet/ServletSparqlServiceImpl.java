package org.aksw.jenax.web.servlet;

import java.io.InputStream;

import org.aksw.jenax.stmt.core.SparqlStmtParser;
import org.apache.jena.rdfconnection.RDFConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


/**
 * This class relies on (spring's) dependency injection in order to
 * get a sparql parser and a connection factory injected.
 *
 * @author raven
 *
 */
@Path("/")
public class ServletSparqlServiceImpl
    extends SparqlEndpointBase
{
    @Context
    protected HttpServletRequest req;

    /** The connection factory is mandatory. It creates RDFConnections from the http request. */
    @Autowired
    protected RdfConnectionFactory sparqlConnectionFactory;

    /** The parser defaults to jena's arq parser */
    @Autowired(required=false)
    protected SparqlStmtParser sparqlStmtParser;


    public ServletSparqlServiceImpl() {
    }

    @Override
    protected SparqlStmtParser getSparqlStmtParser() {
        SparqlStmtParser result = sparqlStmtParser != null ? sparqlStmtParser : super.getSparqlStmtParser();
        return result;
    };

    @Override
    protected RDFConnection getConnection() {
        RDFConnection result = sparqlConnectionFactory.getConnection(req);
        return result;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response executeRequestXml()
            throws Exception {

        // InputStream r = new ClassPathResource("snorql/index.html").getInputStream();
        InputStream r = new ClassPathResource("static/yasgui/index.html").getInputStream();
        // System.out.println(IOUtils.toString(r, StandardCharsets.UTF_8));
        Response result;
        if(r == null) {
            result = Response.ok("SPARQL HTML front end not configured", MediaType.TEXT_HTML).build();
        } else {
            result = Response.ok(r, MediaType.TEXT_HTML).build();
        }

//        Response result = Response.status(Status.NOT_FOUND).build();
        return result;
    }
}

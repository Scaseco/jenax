package org.aksw.jenax.web.servlet.graphql.v2;

import java.io.InputStream;

import org.aksw.jenax.graphql.sparql.v2.exec.api.high.GraphQlExecFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class ServletGraphQlEndpointImpl
    extends GraphQlEndpointBase
{
    /** The connection factory is mandatory. It creates RDFConnections from the http request. */
    @Autowired
    protected GraphQlExecFactory graphQlExecFactory;

    @Override
    protected GraphQlExecFactory getGraphQlExecFactory() {
        return graphQlExecFactory;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response executeRequestXml()
            throws Exception {

        // InputStream r = new ClassPathResource("snorql/index.html").getInputStream();
        InputStream r = new ClassPathResource("static/graphql/mui/index.html").getInputStream();
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

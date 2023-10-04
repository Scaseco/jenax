package org.aksw.jenax.web.servlet;

import java.util.Objects;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ConnectionCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.aksw.jenax.graphql.GraphQlExec;
import org.aksw.jenax.graphql.GraphQlExecFactory;
import org.aksw.jenax.graphql.impl.core.GraphQlExecUtils;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.language.Document;
import graphql.parser.Parser;

public abstract class GraphQlEndpointBase {
    private static final Logger logger = LoggerFactory.getLogger(GraphQlEndpointBase.class);

    protected abstract GraphQlExecFactory getGraphQlExecFactory();


    @POST
    public void execute(
            @Suspended AsyncResponse response,
            @Context HttpHeaders headers,
            @FormParam("query") String query) {
        processQueryAsync(response, query);
    }

    public void processQueryAsync(
            AsyncResponse response,
            String query) {


        // RDFConnection conn = getConnection();
        // logger.debug("Opened connection: " + System.identityHashCode(conn));

        GraphQlExec tmp;
        try {
            Parser parser = new Parser();
            Document document = parser.parseDocument(query);
            GraphQlExecFactory gef = Objects.requireNonNull(getGraphQlExecFactory(), "GraphQlExecFactory is null");
            tmp = gef.create(document);
        } catch (Exception e) {
            response.resume(e);
            throw new RuntimeException(e);
        }

        response
        .register(new ConnectionCallback() {
            @Override
            public void onDisconnect(AsyncResponse disconnect) {
                logger.debug("Client disconnected");

                // qe.abort();

//                if(true) {
//                disconnect.resume(
//                    Response.status(Response.Status.SERVICE_UNAVAILABLE)
//                    .entity("Connection Callback").build());
//                } else {
//                    disconnect.cancel();
//                }
            }
        });

        response
        .register(new CompletionCallback() {
            @Override
            public void onComplete(Throwable t) {
                if(t == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Successfully completed graphql query execution");
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed graphql query execution");
                    }
                }
            }
        });

//        response
//        .setTimeoutHandler(new TimeoutHandler() {
//           @Override
//           public void handleTimeout(AsyncResponse asyncResponse) {
//               logger.debug("Timout on request");
//               asyncResponse.resume(
//                   Response.status(Response.Status.SERVICE_UNAVAILABLE)
//                   .entity("Operation time out.").build());
//          }
//        });
//
//        response.setTimeout(600, TimeUnit.SECONDS);


        Response x = processQuery(tmp);
        response.resume(x);
    }

    public Response processQuery(GraphQlExec ge) {
        String contentTypeStr = WebContent.contentTypeJSON;
        StreamingOutput processor = out -> GraphQlExecUtils.write(out, ge);
        Response result = Response.ok(processor, contentTypeStr).build();
        return result;
    }
}

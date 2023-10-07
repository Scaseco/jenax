package org.aksw.jenax.web.servlet;

import java.util.Objects;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ConnectionCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.aksw.jenax.graphql.GraphQlExec;
import org.aksw.jenax.graphql.GraphQlExecFactory;
import org.aksw.jenax.graphql.impl.core.GraphQlExecUtils;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GraphQlEndpointBase {
    private static final Logger logger = LoggerFactory.getLogger(GraphQlEndpointBase.class);

    protected abstract GraphQlExecFactory getGraphQlExecFactory();


    @POST
    @Consumes //(MediaType.APPLICATION_JSON)
    // @Produces(MediaType.APPLICATION_JSON)
    public void execute(
            @Suspended AsyncResponse response,
            // @Context HttpHeaders headers,
            String postBody) {
        processQueryAsync(response, postBody);
    }

    public void processQueryAsync(
            AsyncResponse asyncResponse,
            String query) {

        GraphQlExec exec;
        try {
            GraphQlExecFactory gef = Objects.requireNonNull(getGraphQlExecFactory(), "GraphQlExecFactory is null");
            exec = GraphQlExecUtils.execJson(gef, query);
        } catch (Exception e) {
            asyncResponse.resume(e);
            throw new RuntimeException(e);
        }

        asyncResponse
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

        asyncResponse
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


        Response response = createResponse(exec);
        asyncResponse.resume(response);
    }

    public static Response createResponse(GraphQlExec ge) {
        String contentTypeStr = WebContent.contentTypeJSON;
        StreamingOutput processor = out -> {
            GraphQlExecUtils.writePretty(out, ge);
        };
        Response result = Response.ok(processor, contentTypeStr).build();
        return result;
    }
}

package org.aksw.jenax.web.servlet.graphql.v2;

import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.api.high.GraphQlExec;
import org.aksw.jenax.graphql.sparql.v2.api.high.GraphQlExecBuilder;
import org.aksw.jenax.graphql.sparql.v2.api.high.GraphQlExecFactory;
import org.aksw.jenax.graphql.sparql.v2.api.high.GraphQlExecUtils;
import org.aksw.jenax.graphql.sparql.v2.io.GraphQlJsonUtils;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.CompletionCallback;
import jakarta.ws.rs.container.ConnectionCallback;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

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

        GraphQlExec<String> exec;
        try {
            GraphQlExecFactory factory = Objects.requireNonNull(getGraphQlExecFactory(), "GraphQlProcessorFactory is null");
            GraphQlExecBuilder builder = factory.newBuilder();
            // builder = GraphQlJsonUtils.configureFromJson(builder, query);
            GraphQlJsonUtils.configureFromJson(builder, query);
            exec = builder.buildForJson();
        } catch (Exception e) {
            asyncResponse.resume(e);
            throw new RuntimeException(e);
        }

        asyncResponse
        .register(new ConnectionCallback() {
            @Override
            public void onDisconnect(AsyncResponse disconnect) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Client disconnected");
                }

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

    public static Response createResponse(GraphQlExec<String> ge) {
        String contentTypeStr = WebContent.contentTypeJSON;
        StreamingOutput processor = out -> {
            GraphQlExecUtils.writePretty(out, ge);
        };
        Response result = Response.ok(processor, contentTypeStr).build();
        return result;
    }
}

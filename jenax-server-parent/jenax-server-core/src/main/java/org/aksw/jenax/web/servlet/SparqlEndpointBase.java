package org.aksw.jenax.web.servlet;

import java.io.PrintStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ConnectionCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.aksw.jenax.connection.query.QueryExecutionDecoratorBase;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParser;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.aksw.jenax.stmt.core.SparqlStmtUpdate;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.update.UpdateProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Jersey resource for an abstract SPARQL endpoint based on the AKSW SPARQL API.
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public abstract class SparqlEndpointBase {

    private static final Logger logger = LoggerFactory.getLogger(SparqlEndpointBase.class);

    protected SparqlStmtParser defaultSparqlStmtParser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, true);

    protected SparqlStmtParser getSparqlStmtParser() {
        return defaultSparqlStmtParser;
    }

    protected abstract RDFConnection getConnection();


    @GET
    @Produces(MediaType.APPLICATION_XML)
    public void executeQueryXml(
            @Suspended final AsyncResponse asyncResponse,
            @QueryParam("query") String queryString,
            @QueryParam("update") String updateString) {
        if(queryString == null && updateString == null) {
            StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
            asyncResponse.resume(Response.status(Status.BAD_REQUEST).entity(so).build()); // TODO: Return some error HTTP code
        } else {
            processStmtAsync(asyncResponse, queryString, updateString, ResultsFormat.FMT_RS_XML);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    public void executeQueryXmlPostAsync(
            @Suspended final AsyncResponse asyncResponse,
            @FormParam("query") String queryString,
            @FormParam("update") String updateString) {

        if(queryString == null) {
            queryString = updateString;
        }

        if(queryString == null) {
            StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
            asyncResponse.resume(Response.ok(so).build()); // TODO: Return some error HTTP code
        } else {
            processStmtAsync(asyncResponse, queryString, updateString, ResultsFormat.FMT_RS_XML);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
    public void executeQueryJson(
            @Suspended final AsyncResponse asyncResponse,
            @QueryParam("query") String queryString,
            @QueryParam("update") String updateString) {
        processStmtAsync(asyncResponse, queryString, updateString, ResultsFormat.FMT_RS_JSON);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
    public void executeQueryXmlPost(
            @Suspended final AsyncResponse asyncResponse,
            @FormParam("query") String queryString,
            @FormParam("update") String updateStr) {
        if(queryString == null) {
            queryString = updateStr;
        }
        processStmtAsync(asyncResponse, queryString, updateStr, ResultsFormat.FMT_RS_JSON);
    }

    public void processStmtAsync(final AsyncResponse response, String queryStr, String updateStr, final ResultsFormat format) {
        if(queryStr == null && updateStr == null) {
            throw new RuntimeException("No query/update statement provided");
        }

        if (queryStr != null && updateStr != null) {
            throw new RuntimeException(String.format("Both 'query' and 'update' statement strings provided in a single request; query=%s update=%s", queryStr, updateStr));
        }

        String stmtStr = queryStr != null ? queryStr : updateStr;

        SparqlStmtParser sparqlStmtParser = getSparqlStmtParser();
        SparqlStmt stmt = sparqlStmtParser.apply(stmtStr);

        if(stmt.isQuery()) {
            processQueryAsync(response, stmt.getAsQueryStmt(), format);
        } else if(stmt.isUpdateRequest()) {
            processUpdateAsync(response, stmt.getAsUpdateStmt());
        } else {
            throw new RuntimeException("Unknown request type: " + queryStr);
        }
    }

    public void processQueryAsync(
            AsyncResponse response,
            SparqlStmtQuery stmt,
            ResultsFormat format) {

        RDFConnection conn = getConnection();
        logger.debug("Opened connection: " + System.identityHashCode(conn));

        QueryExecution tmp;
        try {
            tmp = stmt.isParsed()
                ? conn.query(stmt.getQuery())
                : conn.query(stmt.getOriginalString());

        } catch (Exception e) {
            try {
                conn.close();
            } catch (Exception e2) {
                e.addSuppressed(e2);
            }
            response.resume(e);

            return ;
            // throw new RuntimeException(e);
        }

        // Wrap the query execution such that close() also closes the connection
        QueryExecution qe = new QueryExecutionDecoratorBase<QueryExecution>(tmp) {
            @Override
            public void close() {
                try {
                    super.close();
                } finally {
                    conn.close();
                    logger.debug("Closed connection: " + System.identityHashCode(conn));
                }
            }
        };

//        asyncResponse
//        .register(new CompletionCallback() {
//
//            @Override
//            public void onComplete(Throwable arg0) {
//                System.out.println("COMPLETE");
//            }
//        });

        response
        .register(new ConnectionCallback() {
            @Override
            public void onDisconnect(AsyncResponse disconnect) {
                logger.debug("Client disconnected");

                qe.abort();

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
                    logger.debug("Successfully completed query execution");
                } else {
                    logger.debug("Failed query execution");
                }
                // Redundant close
                // qeAndType.getQueryExecution().close();
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

        StreamingOutput result = processQuery(qe, format);
        response.resume(result);

        /*
        ThreadUtils.start(response, new Runnable() {
            @Override
            public void run() {
                try {
                    StreamingOutput result = processQuery(qe, format);
                    response.resume(result);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        */
    }


    @GET
    @Produces("application/rdf+xml") //HttpParams.contentTypeRDFXML)
    public void executeQueryRdfXml(
            @Suspended final AsyncResponse asyncResponse,
            @QueryParam("query") String queryString,
            @QueryParam("update") String updateString
            ) {
        processStmtAsync(asyncResponse, queryString, updateString, ResultsFormat.FMT_RDF_XML);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/rdf+xml")// HttpParams.contentTypeRDFXML)
    public void executeQueryRdfXmlPost(
            @Suspended final AsyncResponse asyncResponse,
            @FormParam("query") String queryString,
            @FormParam("update") String updateString) {
        processStmtAsync(asyncResponse, queryString, updateString, ResultsFormat.FMT_RDF_XML);
    }

    @GET
    @Produces("application/sparql-results+xml")
    public void executeQueryResultSetXml(
            @Suspended final AsyncResponse asyncResponse,
            @QueryParam("query") String queryString,
            @QueryParam("update") String updateString) {
        processStmtAsync(asyncResponse, queryString, updateString, ResultsFormat.FMT_RS_XML);
    }


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/sparql-results+xml")
    public void executeQueryResultSetXmlPost(
            @Suspended final AsyncResponse asyncResponse,
            @FormParam("query") String queryString,
            @FormParam("update") String updateString) {
        processStmtAsync(asyncResponse, queryString, updateString, ResultsFormat.FMT_RS_XML);
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public void executeQueryText(
            @Suspended final AsyncResponse asyncResponse,
            @QueryParam("query") String queryString,
            @QueryParam("update") String updateString) {
        processStmtAsync(asyncResponse, queryString, updateString, ResultsFormat.FMT_TEXT);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public void executeQueryTextPost(
            @Suspended final AsyncResponse asyncResponse,
            @FormParam("query") String queryString,
            @FormParam("update") String updateString) {
        processStmtAsync(asyncResponse, queryString, updateString, ResultsFormat.FMT_TEXT);
    }



    public StreamingOutput processQuery(QueryExecution qe, ResultsFormat resultsFormat)
    {
        return outputStream -> {
            PrintStream ps = new PrintStream(outputStream);
            try (QueryExecution myQe = qe) {
                QueryExecUtils.exec(null, QueryExec.adapt(myQe), resultsFormat, ps);
            }
        };
    }



    /*
     * UPDATE
     */


    @GET
    @Consumes(WebContent.contentTypeSPARQLUpdate)
    @Produces(MediaType.APPLICATION_JSON)
    public void executeUpdateGet(
            @Suspended AsyncResponse asyncResponse,
            @QueryParam("update") String updateString) {
        processStmtAsync(asyncResponse, null, updateString, ResultsFormat.FMT_NONE);
    }



    @POST
    @Consumes(WebContent.contentTypeSPARQLUpdate)
    @Produces(MediaType.APPLICATION_JSON)
    public void executeUpdatePost(
            @Suspended AsyncResponse asyncResponse,
            String updateString) {
        processStmtAsync(asyncResponse, null, updateString, ResultsFormat.FMT_NONE);
    }



//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public void executeUpdateGet(@Suspended final AsyncResponse asyncResponse,
//            @QueryParam("update") String updateRequestStr)
//        throws Exception
//    {
//        processUpdateAsync(asyncResponse, new SparqlStmtUpdate(updateRequestStr));
//    }


//    @POST
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    @Produces(MediaType.APPLICATION_JSON)
//    public void executeUpdatePost(@Suspended final AsyncResponse asyncResponse,
//            @FormParam("update") String updateRequestStr)
//        throws Exception
//    {
//        processUpdateAsync(asyncResponse, new SparqlStmtUpdate(updateRequestStr));
//    }

//    public void executeUpdateAny(@Suspended final AsyncResponse asyncResponse,
//            String serviceUri,
//            String queryString,
//            List<String> usingGraphUris,
//            List<String> usingNamedGraphUris)
//        throws Exception
//    {
//        if(queryString == null) {
//            StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
//            asyncResponse.resume(Response.status(Status.BAD_REQUEST).entity(so).build()); // TODO: Return some error HTTP code
//        } else {
//            processUpdateAsync(asyncResponse, serviceUri, queryString, usingGraphUris, usingNamedGraphUris);
//        }
//    }


//    public UpdateProcessor createUpdateProcessor(String serviceUri, String requestStr, List<String> usingGraphUris, List<String> usingNamedGraphUris) {
//        HttpAuthenticator authenticator = AuthenticatorUtils.parseAuthenticator(req);
//
//        SparqlServiceFactory ssf = getSparqlServiceFactory();
//        UpdateProcessor result = createUpdateProcessor(ssf, serviceUri, requestStr, usingGraphUris, usingNamedGraphUris, authenticator);
//        return result;
//    }
//
//    public UpdateProcessor createUpdateProcessor(String updateRequestStr) {
//        throw new RuntimeException("For update requests, this method must be overriden");
//    }
//
//    public static UpdateProcessor createUpdateProcessor(SparqlServiceFactory ssf, String serviceUri, String requestStr, List<String> usingGraphUris, List<String> usingNamedGraphUris, HttpAuthenticator authenticator) {
//        // TODO Should we use UsingList or DatasetDescription? The latter feels more natural to use.
////      UsingList usingList = new UsingList();
////      usingList.addAllUsing(NodeUtils.convertToNodes(usingGraphUris));
////      usingList.addAllUsingNamed(NodeUtils.convertToNodes(usingNamedGraphUris));
//        DatasetDescription datasetDescription = new DatasetDescription(usingGraphUris, usingNamedGraphUris);
//
//
//        SparqlService sparqlService = ssf.createSparqlService(serviceUri, datasetDescription, authenticator);
//
//        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();
//
//        UpdateRequest updateRequest = UpdateRequestUtils.parse(requestStr);
//        UpdateProcessor result = uef.createUpdateProcessor(updateRequest);
//        return result;
//    }
    public UpdateProcessor createUpdateProcessor(SparqlStmtUpdate stmt) { //UpdateRequest updateRequest);
        throw new UnsupportedOperationException("The method for handling SPARQL update requests has not been overridden");
    }

    public void processUpdateAsync(final AsyncResponse response, SparqlStmtUpdate stmt) { //String serviceUri, String requestStr, List<String> usingGraphUris, List<String> usingNamedGraphUris) {


      response
      .register(new ConnectionCallback() {
          @Override
          public void onDisconnect(AsyncResponse disconnect) {
              logger.debug("Client disconnected");

              // TODO Abort
              //qeAndType.getQueryExecution().abort();

//              if(true) {
//              disconnect.resume(
//                  Response.status(Response.Status.SERVICE_UNAVAILABLE)
//                  .entity("Connection Callback").build());
//              } else {
//                  disconnect.cancel();
//              }
          }
      });

      response
      .register(new CompletionCallback() {
          @Override
          public void onComplete(Throwable t) {
              if(t == null) {
                  logger.debug("Successfully completed query execution");
              } else {
                  logger.debug("Failed query execution");
              }
              //qeAndType.getQueryExecution().close();
              // TODO Close
          }
      });

//      response
//      .setTimeoutHandler(new TimeoutHandler() {
//         @Override
//         public void handleTimeout(AsyncResponse asyncResponse) {
//             logger.debug("Timout on request");
//             asyncResponse.resume(
//                 Response.status(Response.Status.SERVICE_UNAVAILABLE)
//                 .entity("Operation time out.").build());
//        }
//      });
//
//      response.setTimeout(600, TimeUnit.SECONDS);

      try (RDFConnection conn = getConnection()) {
          logger.debug("Opened connection: " + System.identityHashCode(conn));

          if (stmt.isParsed()) {
              conn.update(stmt.getUpdateRequest());
          } else {
              conn.update(stmt.getOriginalString());
          }

          String result = "{\"success\": true}";
          response.resume(result);
      } catch (Exception e) {
          response.resume(e);
      }

  }



}


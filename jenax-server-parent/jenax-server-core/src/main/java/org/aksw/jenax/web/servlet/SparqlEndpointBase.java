package org.aksw.jenax.web.servlet;

import java.io.OutputStream;
import java.util.function.Consumer;

import org.aksw.jenax.arq.util.fmt.SparqlQueryFmtOverResultFmt;
import org.aksw.jenax.arq.util.fmt.SparqlQueryFmts;
import org.aksw.jenax.arq.util.fmt.SparqlQueryFmtsUtils;
import org.aksw.jenax.arq.util.fmt.SparqlResultFmts;
import org.aksw.jenax.arq.util.fmt.SparqlResultFmtsImpl;
import org.aksw.jenax.dataaccess.sparql.execution.query.QueryExecutionWrapperBase;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParser;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.aksw.jenax.stmt.core.SparqlStmtUpdate;
import org.aksw.jenax.stmt.resultset.SPARQLResultEx;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RDFWriterBuilder;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.resultset.ResultSetWriter;
import org.apache.jena.riot.resultset.ResultSetWriterFactory;
import org.apache.jena.riot.resultset.ResultSetWriterRegistry;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.update.UpdateProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.CompletionCallback;
import jakarta.ws.rs.container.ConnectionCallback;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;


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

    protected void executeAsync(AsyncResponse asyncResponse, String acceptHeaders, String queryString, String updateString) {
        AcceptList acceptList = new AcceptList(acceptHeaders);
        SparqlResultFmts fmts = SparqlResultFmtsImpl.forContentTypes(acceptList);
        processStmtAsync(asyncResponse, queryString, updateString, fmts);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    //@Produces({MediaType.TEXT_PLAIN, WebContent.contentTypeNTriples, WebContent.contentTypeTurtle, WebContent.contentTypeTriG})
    public void executeWildcardPost(
            @Suspended AsyncResponse asyncResponse,
            @Context HttpHeaders headers,
            @FormParam("query") String queryString,
            @FormParam("update") String updateStr) {
        executeAsync(asyncResponse, headers.getHeaderString("Accept"), queryString, updateStr);
    }


    @POST
    @Consumes(WebContent.contentTypeSPARQLQuery)
    //@Produces({MediaType.TEXT_PLAIN, WebContent.contentTypeNTriples, WebContent.contentTypeTurtle, WebContent.contentTypeTriG})
    public void executeQueryWildcardPostDirect(
            @Suspended AsyncResponse asyncResponse,
            @Context HttpHeaders headers,
            String queryString) {
        executeAsync(asyncResponse, headers.getHeaderString("Accept"), queryString, null);
    }

    @GET
//    @Produces({MediaType.TEXT_PLAIN, WebContent.contentTypeNTriples, WebContent.contentTypeTurtle, WebContent.contentTypeTriG})
    public void executeQueryText(
            @Suspended AsyncResponse asyncResponse,
            @Context HttpHeaders headers,
            @QueryParam("query") String queryString,
            @QueryParam("update") String updateString) {
        executeAsync(asyncResponse, headers.getHeaderString("Accept"), queryString, updateString);
    }

    @POST
    @Consumes(WebContent.contentTypeSPARQLUpdate)
//    @Produces(MediaType.APPLICATION_JSON)
    public void executeUpdatePost(
            @Suspended AsyncResponse asyncResponse,
            @Context HttpHeaders headers,
            String updateString) {
        executeAsync(asyncResponse, headers.getHeaderString("Accept"), null, updateString);
    }



    public void processStmtAsync(AsyncResponse response, String queryStr, String updateStr, SparqlResultFmts format) {
        if(queryStr == null && updateStr == null) {
            throw new QueryException("No query/update statement provided");
        }

        if (queryStr != null && updateStr != null && !updateStr.equals(queryStr)) {
            throw new QueryException(String.format("Both 'query' and 'update' statement strings provided in a single request; query=%s update=%s", queryStr, updateStr));
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
            SparqlResultFmts format) {

        RDFConnection conn = getConnection();
        if (logger.isDebugEnabled()) {
            logger.debug("Opened connection: " + System.identityHashCode(conn));
        }

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
            throw new RuntimeException(e);
        }

        // Wrap the query execution such that close() also closes the connection
        // Note: QueryExecutionWrapperTxn.wrap is not suitable because not every connection supports it
        QueryExecution qe = new QueryExecutionWrapperBase<QueryExecution>(tmp) {
            @Override
            public void close() {
                try {
                    super.close();
                } finally {
                    conn.close();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Closed connection: " + System.identityHashCode(conn));
                    }
                }
            }
        };

        response.register(new ConnectionCallback() {
            @Override
            public void onDisconnect(AsyncResponse disconnect) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Client disconnected");
                }
                qe.abort();
            }
        });

        response.register(new CompletionCallback() {
            @Override
            public void onComplete(Throwable t) {
                if(t == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Successfully completed query execution");
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed query execution");
                    }
                }
                // Redundant close
                // qeAndType.getQueryExecution().close();
            }
        });

        Response x = processQuery(qe, format);
        // Response x = Response.ok(result).type(mediaType).build();
        response.resume(x);
    }

    public static Consumer<OutputStream> createQueryProcessor(QueryExecution qe, Lang lang, RDFFormat fmt, org.apache.jena.sparql.util.Context cxt) {
        Consumer<OutputStream> emitter = null;
        ResultSetWriterFactory rswf = lang != null ? ResultSetWriterRegistry.getFactory(lang) : null;
        Query q = qe.getQuery();

        // Try to process the query with a result set language
        if (rswf != null) {
            emitter = out -> {
                ResultSetWriter rsWriter = rswf.create(lang);
                SPARQLResultEx sr = SparqlStmtUtils.execAny(qe, q);
                if (sr.isBoolean()) {
                    boolean v = sr.getBooleanResult();
                    rsWriter.write(out, v, cxt);
                } else if (sr.isJson()) {
                    // TODO: set proper json Content-type
                    ResultSetFormatter.output(out, sr.getJsonItems());
                } else {
                    rsWriter.write(out, sr.getResultSet(), cxt);
                }
            };
        } else if (fmt != null) {
            // Try to process the query with a triple / quad language
            if (StreamRDFWriter.registered(fmt)) {
                emitter = out -> {
                    StreamRDF writer = StreamRDFWriter.getWriterStream(out, fmt, cxt);
                    writer.start();

                    SPARQLResultEx sr = SparqlStmtUtils.execAny(qe, null);
                    if (sr.isQuads()) {
                        StreamRDFOps.sendQuadsToStream(sr.getQuads(), writer);
                    } else if (sr.isTriples()) {
                        StreamRDFOps.sendTriplesToStream(sr.getTriples(), writer);
                    }

                    writer.finish();
                };
            } else if (RDFWriterRegistry.contains(lang)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Warning! Running non-streaming RDF writer " + fmt.toString() + " and building in-memory model");
                }
                emitter = out -> {
                    SPARQLResultEx sr = SparqlStmtUtils.execAny(qe, null);
                    RDFWriterBuilder writerBuilder = RDFWriter.create();
                    if (sr.isQuads()) {
                        DatasetGraph dsg = DatasetGraphFactory.createGeneral();
                        sr.getQuads().forEachRemaining(dsg::add);
                        writerBuilder.source(dsg);
                    } else if (sr.isTriples()) {
                        Graph graph = GraphFactory.createPlainGraph();
                        sr.getTriples().forEachRemaining(graph::add);
                        writerBuilder.source(graph);
                    }

                    writerBuilder.format(fmt).context(cxt).output(out);
                };
            } else {
                throw new RuntimeException("Could not handle execution of query " + q + " with lang " + lang);
            }
        }

        // Ensure to always close the query execution once writing the output completes (regardless whether normally or exceptionally)
        Consumer<OutputStream> finalEmitter = emitter;
        Consumer<OutputStream> result = out -> {
            try {
                finalEmitter.accept(out);
            } finally {
                qe.close();
            }
        };

        return result;
    }

    public Response processQuery(QueryExecution qe, SparqlResultFmts format)
    {
        Lang lang = null;
        RDFFormat rdfFormat = null;

        Query query = qe.getQuery();
        if (query != null) {
            SparqlQueryFmts fmts = new SparqlQueryFmtOverResultFmt(format);
            lang = SparqlQueryFmtsUtils.getLang(fmts, query);
            rdfFormat = SparqlQueryFmtsUtils.getRdfFormat(fmts, query);
        }

        Response result;
        if (lang != null) {
            String contentTypeStr = lang.getContentType().getContentTypeStr();
            StreamingOutput processor = createQueryProcessor(qe, lang, rdfFormat, org.apache.jena.sparql.util.Context.create())::accept;
            result = Response.ok(processor, contentTypeStr).build();
        } else {
            // Send the query to the backend and determine the response content type from
            // the backend's content type
            throw new RuntimeException("Cannot handle unparsed query yet");
        }

        return result;
    }

    public UpdateProcessor createUpdateProcessor(SparqlStmtUpdate stmt) { //UpdateRequest updateRequest);
        throw new UnsupportedOperationException("The method for handling SPARQL update requests has not been overridden");
    }

    public void processUpdateAsync(AsyncResponse response, SparqlStmtUpdate stmt) { //String serviceUri, String requestStr, List<String> usingGraphUris, List<String> usingNamedGraphUris) {
      RDFConnection conn = getConnection();

      response.register(new ConnectionCallback() {
          @Override
          public void onDisconnect(AsyncResponse disconnect) {
              conn.abort();
              if (logger.isDebugEnabled()) {
                  logger.debug("Client disconnected");
              }
          }
      });

      response.register(new CompletionCallback() {
          @Override
          public void onComplete(Throwable t) {
              if(t == null) {
                  if (logger.isDebugEnabled()) {
                      logger.debug("Successfully completed query execution");
                  }
              } else {
                  if (logger.isDebugEnabled()) {
                      logger.debug("Failed query execution");
                  }
              }
          }
      });

      try {
          if (logger.isDebugEnabled()) {
              logger.debug("Opened connection: " + System.identityHashCode(conn));
          }
//          Txn.execute(conn, () -> {
              if (stmt.isParsed()) {
                  conn.update(stmt.getUpdateRequest());
              } else {
                  conn.update(stmt.getOriginalString());
              }
//          });
          String result = "{\"success\": true}";
          response.resume(result);
      } catch (Exception e) {
          response.resume(e);
      } finally {
          conn.close();
      }
  }
}



//@POST
//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//@Produces(MediaType.TEXT_PLAIN)
//public void executeQueryTextPost(
//      @Suspended AsyncResponse asyncResponse,
//      @FormParam("query") String queryString,
//      @FormParam("update") String updateString) {
//  processStmtAsync(asyncResponse, queryString, updateString, SparqlResultFmtsImpl.TXT);
//}




//@GET
//@Produces(MediaType.APPLICATION_JSON)
//public void executeUpdateGet(@Suspended final AsyncResponse asyncResponse,
//      @QueryParam("update") String updateRequestStr)
//  throws Exception
//{
//  processUpdateAsync(asyncResponse, new SparqlStmtUpdate(updateRequestStr));
//}


//@POST
//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//@Produces(MediaType.APPLICATION_JSON)
//public void executeUpdatePost(@Suspended final AsyncResponse asyncResponse,
//      @FormParam("update") String updateRequestStr)
//  throws Exception
//{
//  processUpdateAsync(asyncResponse, new SparqlStmtUpdate(updateRequestStr));
//}

//public void executeUpdateAny(@Suspended final AsyncResponse asyncResponse,
//      String serviceUri,
//      String queryString,
//      List<String> usingGraphUris,
//      List<String> usingNamedGraphUris)
//  throws Exception
//{
//  if(queryString == null) {
//      StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
//      asyncResponse.resume(Response.status(Status.BAD_REQUEST).entity(so).build()); // TODO: Return some error HTTP code
//  } else {
//      processUpdateAsync(asyncResponse, serviceUri, queryString, usingGraphUris, usingNamedGraphUris);
//  }
//}


//public UpdateProcessor createUpdateProcessor(String serviceUri, String requestStr, List<String> usingGraphUris, List<String> usingNamedGraphUris) {
//  HttpAuthenticator authenticator = AuthenticatorUtils.parseAuthenticator(req);
//
//  SparqlServiceFactory ssf = getSparqlServiceFactory();
//  UpdateProcessor result = createUpdateProcessor(ssf, serviceUri, requestStr, usingGraphUris, usingNamedGraphUris, authenticator);
//  return result;
//}
//
//public UpdateProcessor createUpdateProcessor(String updateRequestStr) {
//  throw new RuntimeException("For update requests, this method must be overriden");
//}
//
//public static UpdateProcessor createUpdateProcessor(SparqlServiceFactory ssf, String serviceUri, String requestStr, List<String> usingGraphUris, List<String> usingNamedGraphUris, HttpAuthenticator authenticator) {
//  // TODO Should we use UsingList or DatasetDescription? The latter feels more natural to use.
////UsingList usingList = new UsingList();
////usingList.addAllUsing(NodeUtils.convertToNodes(usingGraphUris));
////usingList.addAllUsingNamed(NodeUtils.convertToNodes(usingNamedGraphUris));
//  DatasetDescription datasetDescription = new DatasetDescription(usingGraphUris, usingNamedGraphUris);
//
//
//  SparqlService sparqlService = ssf.createSparqlService(serviceUri, datasetDescription, authenticator);
//
//  UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();
//
//  UpdateRequest updateRequest = UpdateRequestUtils.parse(requestStr);
//  UpdateProcessor result = uef.createUpdateProcessor(updateRequest);
//  return result;
//}


//@GET
//@Produces({MediaType.APPLICATION_JSON, WebContent.contentTypeResultsJSON})
//public void executeQueryJson(
//      @Suspended AsyncResponse asyncResponse,
//      @QueryParam("query") String queryString,
//      @QueryParam("update") String updateString) {
//  processStmtAsync(asyncResponse, queryString, updateString, SparqlResultFmtsImpl.JSON);
//}
//
//@POST
//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//@Produces({MediaType.APPLICATION_JSON, WebContent.contentTypeResultsJSON})
//public void executeQueryJsonPost(
//      @Suspended AsyncResponse asyncResponse,
//      @FormParam("query") String queryString,
//      @FormParam("update") String updateStr) {
//  if(queryString == null) {
//      queryString = updateStr;
//  }
//  processStmtAsync(asyncResponse, queryString, updateStr, SparqlResultFmtsImpl.JSON);
//}
//
//@POST
//@Consumes(WebContent.contentTypeSPARQLQuery)
//@Produces({MediaType.APPLICATION_JSON, WebContent.contentTypeResultsJSON})
//public void executeQueryJsonPostDirect(
//      @Suspended AsyncResponse asyncResponse,
//      String queryString) {
//  processStmtAsync(asyncResponse, queryString, null, SparqlResultFmtsImpl.JSON);
//}
//
//@GET
//@Produces(MediaType.APPLICATION_XML)
//public void executeQueryXml(
//      @Suspended AsyncResponse asyncResponse,
//      @QueryParam("query") String queryString,
//      @QueryParam("update") String updateString) {
//  if(queryString == null && updateString == null) {
//      StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
//      asyncResponse.resume(Response.status(Status.BAD_REQUEST).entity(so).build()); // TODO: Return some error HTTP code
//  } else {
//      processStmtAsync(asyncResponse, queryString, updateString, SparqlResultFmtsImpl.XML);
//  }
//}
//
//@POST
//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//@Produces(MediaType.APPLICATION_XML)
//public void executeQueryXmlPostAsync(
//      @Suspended AsyncResponse asyncResponse,
//      @FormParam("query") String queryString,
//      @FormParam("update") String updateString) {
//
//  if(queryString == null) {
//      queryString = updateString;
//  }
//
//  if(queryString == null) {
//      StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
//      asyncResponse.resume(Response.ok(so).build()); // TODO: Return some error HTTP code
//  } else {
//      processStmtAsync(asyncResponse, queryString, updateString, SparqlResultFmtsImpl.XML);
//  }
//}
//
//@GET
//@Produces("application/rdf+xml") //HttpParams.contentTypeRDFXML)
//public void executeQueryRdfXml(
//      @Suspended AsyncResponse asyncResponse,
//      @QueryParam("query") String queryString,
//      @QueryParam("update") String updateString
//      ) {
//  processStmtAsync(asyncResponse, queryString, updateString, SparqlResultFmtsImpl.XML);
//}
//
//@POST
//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//@Produces("application/rdf+xml")// HttpParams.contentTypeRDFXML)
//public void executeQueryRdfXmlPost(
//      @Suspended AsyncResponse asyncResponse,
//      @FormParam("query") String queryString,
//      @FormParam("update") String updateString) {
//  processStmtAsync(asyncResponse, queryString, updateString, SparqlResultFmtsImpl.XML);
//}
//
//@POST
//@Consumes(WebContent.contentTypeSPARQLQuery)
//@Produces("application/rdf+xml")
//public void executeQueryRdfXmlPostDirect(
//      @Suspended AsyncResponse asyncResponse,
//      String queryString) {
//  processStmtAsync(asyncResponse, queryString, null, SparqlResultFmtsImpl.XML);
//}
//
//
//@GET
//@Produces(WebContent.contentTypeResultsXML)
//public void executeQueryResultSetXml(
//      @Suspended AsyncResponse asyncResponse,
//      @QueryParam("query") String queryString,
//      @QueryParam("update") String updateString) {
//  processStmtAsync(asyncResponse, queryString, updateString, SparqlResultFmtsImpl.XML);
//}
//
//@POST
//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//@Produces(WebContent.contentTypeResultsXML)
//public void executeQueryResultSetXmlPost(
//      @Suspended AsyncResponse asyncResponse,
//      @FormParam("query") String queryString,
//      @FormParam("update") String updateString) {
//  processStmtAsync(asyncResponse, queryString, updateString, SparqlResultFmtsImpl.XML);
//}
//
//@POST
//@Consumes(WebContent.contentTypeSPARQLQuery)
//@Produces(WebContent.contentTypeResultsXML)
//public void executeQueryResultSetXmlPostDirect(
//      @Suspended AsyncResponse asyncResponse,
//      String queryString) {
//  processStmtAsync(asyncResponse, queryString, null, SparqlResultFmtsImpl.XML);
//}
//
//@GET
//@Produces(WebContent.contentTypeTextCSV)
//public void executeQueryResultSetCsv(
//      @Suspended AsyncResponse asyncResponse,
//      @QueryParam("query") String queryString,
//      @QueryParam("update") String updateString) {
//  processStmtAsync(asyncResponse, queryString, updateString, SparqlResultFmtsImpl.createCsv());
//}
//
//@POST
//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//@Produces(WebContent.contentTypeTextCSV)
//public void executeQueryResultSetCsvPost(
//      @Suspended AsyncResponse asyncResponse,
//      @FormParam("query") String queryString,
//      @FormParam("update") String updateString) {
//  processStmtAsync(asyncResponse, queryString, updateString, SparqlResultFmtsImpl.createCsv());
//}
//
//@POST
//@Consumes(WebContent.contentTypeSPARQLQuery)
//@Produces(WebContent.contentTypeTextCSV)
//public void executeQueryResultSetCsvPostDirect(
//      @Suspended AsyncResponse asyncResponse,
//      String queryString) {
//  processStmtAsync(asyncResponse, queryString, null, SparqlResultFmtsImpl.createCsv());
//}
//
//
//@GET
//@Produces(WebContent.contentTypeTextTSV)
//public void executeQueryResultSetTsv(
//      @Suspended AsyncResponse asyncResponse,
//      @QueryParam("query") String queryString,
//      @QueryParam("update") String updateString) {
//  processStmtAsync(asyncResponse, queryString, updateString, SparqlResultFmtsImpl.createTsv());
//}
//
//@POST
//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//@Produces(WebContent.contentTypeTextTSV)
//public void executeQueryResultSetTsvPost(
//      @Suspended AsyncResponse asyncResponse,
//      @FormParam("query") String queryString,
//      @FormParam("update") String updateString) {
//  processStmtAsync(asyncResponse, queryString, updateString, SparqlResultFmtsImpl.createTsv());
//}
//
//@POST
//@Consumes(WebContent.contentTypeSPARQLQuery)
//@Produces(WebContent.contentTypeTextTSV)
//public void executeQueryResultSetTsvPostDirect(
//      @Suspended AsyncResponse asyncResponse,
//      String queryString) {
//  processStmtAsync(asyncResponse, queryString, null, SparqlResultFmtsImpl.createTsv());
//}


/*
* UPDATE
*/


//@GET
//@Consumes(WebContent.contentTypeSPARQLUpdate)
//@Produces(MediaType.APPLICATION_JSON)
//public void executeUpdateGet(
//      @Suspended AsyncResponse asyncResponse,
//      @QueryParam("update") String updateString) {
//  processStmtAsync(asyncResponse, null, updateString, null);
//}

//response
//.setTimeoutHandler(new TimeoutHandler() {
// @Override
// public void handleTimeout(AsyncResponse asyncResponse) {
//     logger.debug("Timout on request");
//     asyncResponse.resume(
//         Response.status(Response.Status.SERVICE_UNAVAILABLE)
//         .entity("Operation time out.").build());
//}
//});
//
//response.setTimeout(600, TimeUnit.SECONDS);
// TODO Abort
//qeAndType.getQueryExecution().abort();

//if(true) {
//disconnect.resume(
//  Response.status(Response.Status.SERVICE_UNAVAILABLE)
//  .entity("Connection Callback").build());
//} else {
//  disconnect.cancel();
//}

// response.resume(result);

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

//response
//.setTimeoutHandler(new TimeoutHandler() {
// @Override
// public void handleTimeout(AsyncResponse asyncResponse) {
//     logger.debug("Timout on request");
//     asyncResponse.resume(
//         Response.status(Response.Status.SERVICE_UNAVAILABLE)
//         .entity("Operation time out.").build());
//}
//});
//
//response.setTimeout(600, TimeUnit.SECONDS);


//if (resultsFormat == null) {
//	Query query = qe.getQuery();
//	if (query != null) {
//		SparqlQueryFmts fmts = new SparqlQueryFmtOverResultFmt(SparqlResultFmtsImpl.createDefault());
//		lang = SparqlQueryFmtsUtils.getLang(fmts, query);
//	}
//} else {
//	lang = ResultsFormat.convert(resultsFormat);
//}
//


package org.aksw.jenax.web.servlet;

import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.aksw.jenax.arq.util.dataset.DatasetDescriptionUtils;
import org.aksw.jenax.arq.util.fmt.SparqlQueryFmtOverResultFmt;
import org.aksw.jenax.arq.util.fmt.SparqlQueryFmts;
import org.aksw.jenax.arq.util.fmt.SparqlQueryFmtsUtils;
import org.aksw.jenax.arq.util.fmt.SparqlResultFmts;
import org.aksw.jenax.arq.util.fmt.SparqlResultFmtsImpl;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParser;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.aksw.jenax.stmt.core.SparqlStmtUpdate;
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
import org.apache.jena.sparql.core.DatasetDescription;
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

    protected void executeAsync(AsyncResponse asyncResponse, String acceptHeaders, String queryString, String updateString,
            List<String> graphIris, List<String> namedGraphIris,
            List<String> usingGraphIris, List<String> usingNamedGraphIris) {
        AcceptList acceptList = new AcceptList(acceptHeaders);
        SparqlResultFmts fmts = SparqlResultFmtsImpl.forContentTypes(acceptList);
        DatasetDescription queryDd = DatasetDescriptionUtils.ofStrings(graphIris, namedGraphIris);
        DatasetDescription updateDd = DatasetDescriptionUtils.ofStrings(usingGraphIris, usingNamedGraphIris);
        processStmtAsync(asyncResponse, queryString, updateString, fmts, queryDd, updateDd);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    //@Produces({MediaType.TEXT_PLAIN, WebContent.contentTypeNTriples, WebContent.contentTypeTurtle, WebContent.contentTypeTriG})
    public void executeWildcardPost(
            @Suspended AsyncResponse asyncResponse,
            @Context HttpHeaders headers,
            @FormParam("query") String queryString,
            @FormParam("update") String updateStr,
            @FormParam("default-graph-uri") List<String> defaultGraphIris,
            @FormParam("named-graph-uri") List<String> namedGraphIris,
            @FormParam("using-graph-uri") List<String> usingGraphIris,
            @FormParam("using-graph-uri") List<String> usingNamedGraphIris) {
        executeAsync(asyncResponse, headers.getHeaderString("Accept"), queryString, updateStr, defaultGraphIris, namedGraphIris, usingGraphIris, usingNamedGraphIris);
    }


    @POST
    @Consumes(WebContent.contentTypeSPARQLQuery)
    //@Produces({MediaType.TEXT_PLAIN, WebContent.contentTypeNTriples, WebContent.contentTypeTurtle, WebContent.contentTypeTriG})
    public void executeQueryWildcardPostDirect(
            @Suspended AsyncResponse asyncResponse,
            @Context HttpHeaders headers,
            String queryString) {
        executeAsync(asyncResponse, headers.getHeaderString("Accept"), queryString, null, null, null, null, null);
    }

    @GET
//    @Produces({MediaType.TEXT_PLAIN, WebContent.contentTypeNTriples, WebContent.contentTypeTurtle, WebContent.contentTypeTriG})
    public void executeQueryText(
            @Suspended AsyncResponse asyncResponse,
            @Context HttpHeaders headers,
            @QueryParam("query") String queryString,
            @QueryParam("update") String updateString,
            @QueryParam("default-graph-uri") List<String> defaultGraphIris,
            @QueryParam("named-graph-uri") List<String> namedGraphIris,
            @QueryParam("using-graph-uri") List<String> usingGraphIris,
            @QueryParam("using-named-graph-uri") List<String> usingNamedGraphIris) {
        executeAsync(asyncResponse, headers.getHeaderString("Accept"), queryString, updateString, defaultGraphIris, namedGraphIris, usingGraphIris, usingNamedGraphIris);
    }

    @POST
    @Consumes(WebContent.contentTypeSPARQLUpdate)
//    @Produces(MediaType.APPLICATION_JSON)
    public void executeUpdatePost(
            @Suspended AsyncResponse asyncResponse,
            @Context HttpHeaders headers,
            String updateString) {
        executeAsync(asyncResponse, headers.getHeaderString("Accept"), null, updateString, null, null, null, null);
    }

    public void processStmtAsync(AsyncResponse response, String queryStr, String updateStr, SparqlResultFmts format, DatasetDescription queryDd, DatasetDescription updateDd) {
        if(queryStr == null && updateStr == null) {
            throw new QueryException("No query/update statement provided");
        }

        if (queryStr != null && updateStr != null && !updateStr.equals(queryStr)) {
            throw new QueryException(String.format("Both 'query' and 'update' statement strings provided in a single request; query=%s update=%s", queryStr, updateStr));
        }

        String stmtStr = queryStr != null ? queryStr : updateStr;

        SparqlStmtParser sparqlStmtParser = getSparqlStmtParser();
        SparqlStmt stmt = sparqlStmtParser.apply(stmtStr);

        if (stmt.isParsed()) {
            if (stmt.isQuery()) {
                SparqlStmtUtils.overwriteDatasetDescription(stmt, queryDd);
            } else if (stmt.isUpdateRequest()) {
                SparqlStmtUtils.overwriteDatasetDescription(stmt, updateDd);
            }
        }

        if(stmt.isQuery()) {
            processQueryAsync(response, stmt.getAsQueryStmt(), format);
        } else if(stmt.isUpdateRequest()) {
            processUpdateAsync(response, stmt.getAsUpdateStmt());
        } else {
            throw new RuntimeException("Unknown request type: " + queryStr);
        }
    }

    public void processQueryAsync(AsyncResponse response, SparqlStmtQuery stmt, SparqlResultFmts format) {
        Object abortLock = new Object();
        boolean[] isAborted = { false };
        QueryExecution[] activeQe = { null };

        Consumer<QueryExecution> qeCallback = qe -> {
            synchronized (abortLock) {
                activeQe[0] = qe;
                if (isAborted[0]) {
                    qe.abort();
                }
            }
        };

        response.register(new ConnectionCallback() {
            @Override
            public void onDisconnect(AsyncResponse disconnect) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Client disconnected");
                }
                synchronized (abortLock) {
                    isAborted[0] = true;
                    QueryExecution qe = activeQe[0];
                    if (qe != null) {
                        qe.abort();
                    }
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
                // Redundant close
                // qeAndType.getQueryExecution().close();
            }
        });

        // Set up a QueryExecutionFactory view which runs each query on
        // its own connection and closes the connection upon termination of the QueryExecution
        RDFDataSource dataSource = () -> getConnection();
        QueryExecutionFactory qef = dataSource.asQef();

        Response x = processQuery(qef, stmt, format, qeCallback);
        // Response x = Response.ok(result).type(mediaType).build();
        response.resume(x);
    }

    public static QueryExecution exec(QueryExecutionFactory qef, SparqlStmtQuery stmt) {
        QueryExecution result;
        if (stmt.isParsed()) {
            Query query = stmt.getQuery();
            result = qef.createQueryExecution(query);
        } else {
            String queryStr = stmt.getOriginalString();
            result = qef.createQueryExecution(queryStr);
        }
        return result;
    }

    /**
     * Return a lambda that executes the query and writes the result to the output stream.
     *
     */
    public static Consumer<OutputStream> createQueryProcessor(QueryExecutionFactory qef, SparqlStmtQuery stmt, Lang lang, RDFFormat fmt, org.apache.jena.sparql.util.Context riotCxt,
            Consumer<QueryExecution> qeCallback) {
        Consumer<OutputStream> result = null;
        ResultSetWriterFactory rswf = lang != null ? ResultSetWriterRegistry.getFactory(lang) : null;

        Query parsedQuery = stmt.isParsed() ? stmt.getQuery() : null;

        if (rswf != null) {
            // Try to process the query with a result set language
            result = out -> {
                try (QueryExecution qe = exec(qef, stmt)) {
                    qeCallback.accept(qe);
                    ResultSetWriter rsWriter = rswf.create(lang);
                    Objects.requireNonNull(rsWriter);
                    // SPARQLResultEx sr = SparqlStmtUtils.execAny(qe, parsedQuery);
                    if (parsedQuery.isAskType()) {
                        boolean v = qe.execAsk();
                        rsWriter.write(out, v, riotCxt);
                    } else if (parsedQuery.isJsonType()) {
                        // TODO: set proper json Content-type
                        ResultSetFormatter.output(out, qe.execJsonItems());
                    } else {
                        rsWriter.write(out, qe.execSelect(), riotCxt);
                    }
                }
            };
        } else if (fmt != null) {
            // Try to process the query with a triple / quad language
            if (StreamRDFWriter.registered(fmt)) {
                result = out -> {
                    StreamRDF writer = StreamRDFWriter.getWriterStream(out, fmt, riotCxt);
                    Objects.requireNonNull(writer);
                    writer.start();
                    try (QueryExecution qe = exec(qef, stmt)) {
                        qeCallback.accept(qe);
                        // SPARQLResultEx sr = SparqlStmtUtils.execAny(qe, parsedQuery);
                        if (parsedQuery.isConstructType()) {
                            if (parsedQuery.isConstructQuad()) {
                                StreamRDFOps.sendQuadsToStream(qe.execConstructQuads(), writer);
                            } else {
                                StreamRDFOps.sendTriplesToStream(qe.execConstructTriples(), writer);
                            }
                        } else if (parsedQuery.isDescribeType()) {
                            StreamRDFOps.sendTriplesToStream(qe.execDescribeTriples(), writer);
                        } else {
                            throw new RuntimeException("Unknown query type: " + parsedQuery);
                        }

                        writer.finish();
                    }
                };
            } else if (RDFWriterRegistry.contains(lang)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Warning! Running non-streaming RDF writer " + fmt.toString() + " and building in-memory model");
                }
                result = out -> {
                    try (QueryExecution qe = exec(qef, stmt)) {
                        qeCallback.accept(qe);
                        // SPARQLResultEx sr = SparqlStmtUtils.execAny(qe, parsedQuery);
                        RDFWriterBuilder writerBuilder = RDFWriter.create();
                        if (parsedQuery.isConstructType()) {
                            if (parsedQuery.isConstructQuad()) {
                                DatasetGraph dsg = DatasetGraphFactory.createGeneral();
                                // sr.getQuads().forEachRemaining(dsg::add);
                                qe.execConstructQuads().forEachRemaining(dsg::add);
                                writerBuilder.source(dsg);
                            } else { // if (sr.isTriples()) {
                                Graph graph = GraphFactory.createPlainGraph();
                                // sr.getTriples().forEachRemaining(graph::add);
                                qe.execConstructTriples().forEachRemaining(graph::add);
                                writerBuilder.source(graph);
                            }
                        } else if (parsedQuery.isDescribeType()) {
                            Graph graph = GraphFactory.createPlainGraph();
                            // sr.getTriples().forEachRemaining(graph::add);
                            qe.execDescribeTriples().forEachRemaining(graph::add);
                            writerBuilder.source(graph);
                        } else {
                            throw new RuntimeException("Unknown query type: " + parsedQuery);
                        }

                        writerBuilder.format(fmt).context(riotCxt).output(out);
                    }
                };
            } else {
                throw new RuntimeException("Could not handle execution of query " + stmt + " with lang " + lang);
            }
        }
        return result;
    }

    public Response processQuery(QueryExecutionFactory qef, SparqlStmtQuery stmt, SparqlResultFmts format, Consumer<QueryExecution> qeCallback)
    {
        Lang lang = null;
        RDFFormat rdfFormat = null;

        Query query = stmt.getQuery();
        if (query != null) {
            SparqlQueryFmts fmts = new SparqlQueryFmtOverResultFmt(format);
            lang = SparqlQueryFmtsUtils.getLang(fmts, query);
            rdfFormat = SparqlQueryFmtsUtils.getRdfFormat(fmts, query);
        }

        Response result;
        if (lang != null) {
            String contentTypeStr = lang.getContentType().getContentTypeStr();
            StreamingOutput processor = createQueryProcessor(qef, stmt , lang, rdfFormat, org.apache.jena.sparql.util.Context.create(), qeCallback)::accept;
            result = Response.ok(processor, contentTypeStr).build();
        } else {
            // Send the query to the backend and determine the response content type from
            // the backend's content type
            // throw new RuntimeException("Cannot handle unparsed query yet");
            throw new RuntimeException("Parse error: ", stmt.getParseException());
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
//protected QueryExecution createQueryExecution(AsyncResponse response,
//SparqlStmtQuery stmt,
//SparqlResultFmts format) {
//
//RDFConnection conn = getConnection();
//if (logger.isDebugEnabled()) {
//logger.debug("Opened connection: " + System.identityHashCode(conn));
//}
//
//QueryExecution tmp;
//try {
//tmp = stmt.isParsed()
//  ? conn.query(stmt.getQuery())
//  : conn.query(stmt.getOriginalString());
//
//} catch (Exception e) {
//try {
//  conn.close();
//} catch (Exception e2) {
//  e.addSuppressed(e2);
//}
//response.resume(e);
//throw new RuntimeException(e);
//}
//
//// Wrap the query execution such that close() also closes the connection
//// Note: QueryExecutionWrapperTxn.wrap is not suitable because not every connection supports it
//QueryExecution qe = new QueryExecutionWrapperBase<QueryExecution>(tmp) {
//@Override
//public void close() {
//  try {
//      super.close();
//  } finally {
//      conn.close();
//      if (logger.isDebugEnabled()) {
//          logger.debug("Closed connection: " + System.identityHashCode(conn));
//      }
//  }
//}
//};
//
//return qe;
//}
//} catch (Exception e) {
//try {
//  conn.close();
//} catch (Exception e2) {
//  e.addSuppressed(e2);
//}
//response.resume(e);
//throw new RuntimeException(e);
//

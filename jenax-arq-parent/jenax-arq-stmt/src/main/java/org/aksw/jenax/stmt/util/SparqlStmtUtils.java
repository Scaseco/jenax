package org.aksw.jenax.stmt.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.exception.HttpExceptionUtils;
import org.aksw.jenax.arq.util.node.NodeEnvsubst;
import org.aksw.jenax.arq.util.node.NodeTransformCollectNodes;
import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.aksw.jenax.arq.util.syntax.ElementTransformSubst2;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.update.UpdateRequestUtils;
import org.aksw.jenax.arq.util.update.UpdateUtils;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParser;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.aksw.jenax.stmt.core.SparqlStmtUpdate;
import org.aksw.jenax.stmt.resultset.SPARQLResultEx;
import org.aksw.jenax.stmt.resultset.SPARQLResultVisitor;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.http.HttpOp;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.modify.request.UpdateData;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;
import org.apache.jena.sparql.syntax.syntaxtransform.UpdateTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateExecution;
import org.apache.jena.update.UpdateRequest;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

/**
 * Utility methods for processing sources of SparqlStmts
 *
 * TODO Consolidate with SparqlStmtMgr
 *
 * @author raven
 *
 */
public class SparqlStmtUtils {

    // TODO Duplicate symbol definition; exists in E_Benchmark
    public static final Symbol symConnection = Symbol.create("http://jsa.aksw.org/connection");

    public static Set<Node> mentionedNodes(SparqlStmt stmt) {
        NodeTransformCollectNodes xform = new NodeTransformCollectNodes();
        applyNodeTransform(stmt, xform);
        Set<Node> result = xform.getNodes();
        return result;
    }

//    public static PrefixMapping getPrefixMapping(SparqlStmt stmt) {
//    	PrefixMapping result = null;
//    	if (stmt.isParsed()) {
//	    	if (stmt.isQuery()) {
//	    		result = stmt.getQuery().getPrefixMapping();
//	    	} else if (stmt.isUpdateRequest()) {
//	    		result = stmt.getUpdateRequest().getPrefixMapping();
//	    	}
//    	}
//    	return result;
//    }

    public static Map<String, Boolean> mentionedEnvVars(SparqlStmt stmt) {
        NodeTransformCollectNodes xform = new NodeTransformCollectNodes();
        applyNodeTransform(stmt, xform);
        Set<Node> nodes = xform.getNodes();
        Map<String, Boolean> result = nodes.stream()
            .map(NodeEnvsubst::getEnvKey)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return result;
    }

    /** Check for this specific QueryParseException - often occurs when attempting to parse a file name */
    public static boolean isEncounteredSlashException(Throwable t) {
        boolean result = false;
        if(t instanceof QueryParseException) {
            QueryParseException qpe = (QueryParseException)t;
            result = Optional.ofNullable(qpe.getMessage())
                    .orElse("").contains("Encountered: \"/\"");
        }

        return result;
    }


    /**
     * For the given collection of SparqlStmts yield the set of used projection vars
     * in select queries
     *
     * This can be used to compose a union result set from multiple separate queries
     *
     * @param stmts
     * @return
     */
    public static List<Var> getUnionProjectVars(Collection<? extends SparqlStmt> stmts) {
        List<Query> selectQueries = stmts.stream()
                .filter(SparqlStmt::isQuery)
                .map(SparqlStmt::getQuery)
                .filter(Query::isSelectType)
                .collect(Collectors.toList());

        // Create the union of variables used in select queries
        Set<Var> result = new LinkedHashSet<>();
        for(Query query : selectQueries) {
            List<Var> varContrib = query.getProjectVars();
            result.addAll(varContrib);
        }

        return new ArrayList<>(result);
    }

    /**
     * Removes all unused prefixes from a stmt.
     *
     * Currently the change happens in-place.
     * TODO optimizePrefixes should not modify in-place because it desyncs with the stmts's original string
     *
     * @param stmt
     * @return
     */
    public static SparqlStmt optimizePrefixes(SparqlStmt stmt) {
        optimizePrefixes(stmt, null);
        return stmt;
    }

    /**
     * In-place optimize a parsed sparql statement's prefixes to only used prefixes.
     * If the statement is not parsed then this operation does nothing!
     * The global prefix map may be null.
     *
     *
     * @param stmt
     * @param globalPm
     * @return
     */
    public static SparqlStmt optimizePrefixes(SparqlStmt stmt, PrefixMapping globalPm) {
        if (stmt.isParsed()) {
            if(stmt.isQuery()) {
                QueryUtils.optimizePrefixes(stmt.getQuery(), globalPm);
            } else if(stmt.isUpdateRequest()) {
                UpdateRequestUtils.optimizePrefixes(stmt.getUpdateRequest(), globalPm);
            }
        }
        return stmt;
    }

    public static SparqlStmt applyElementTransform(SparqlStmt stmt, Function<? super Element, ? extends Element> transform) {
        SparqlStmt result;
        if(stmt.isQuery()) {
            Query tmp = stmt.getAsQueryStmt().getQuery();
            Query query = QueryUtils.applyElementTransform(tmp, transform);
            result = new SparqlStmtQuery(query);
        } else if(stmt.isUpdateRequest()) {
            UpdateRequest tmp = stmt.getAsUpdateStmt().getUpdateRequest();
            UpdateRequest updateRequest = UpdateRequestUtils.applyTransformElt(tmp, transform);
            result = new SparqlStmtUpdate(updateRequest);
        } else {
            result = stmt;
        }
        return result;
    }

    public static SparqlStmt applyOpTransform(SparqlStmt stmt, Transform transform) {
        return applyOpTransform(stmt, op -> Transformer.transform(transform, op));
    }

    public static SparqlStmt applyOpTransform(SparqlStmt stmt, Function<? super Op, ? extends Op> transform) {
        SparqlStmt result;
        if(stmt.isQuery()) {
            Query tmp = stmt.getAsQueryStmt().getQuery();
            Query query = QueryUtils.applyOpTransform(tmp, transform);
            result = new SparqlStmtQuery(query);
        } else if(stmt.isUpdateRequest()) {
            UpdateRequest tmp = stmt.getAsUpdateStmt().getUpdateRequest();
            UpdateRequest updateRequest = UpdateRequestUtils.applyOpTransform(tmp, transform);

            result = new SparqlStmtUpdate(updateRequest);
        } else {
            result = stmt;
        }
        return result;
    }

    public static SparqlStmt applyNodeTransform(SparqlStmt stmt, NodeTransform xform) {
        SparqlStmt result;

        ElementTransform elform = new ElementTransformSubst2(xform);
        ExprTransform exform = new ExprTransformNodeElement(xform, elform);

        if(stmt.isQuery()) {
            Query before = stmt.getAsQueryStmt().getQuery();
//			Op beforeOp = Algebra.compile(before);
//			Op afterOp = NodeTransformLib.transform(xform, beforeOp);

//			NodeTransformLib.transform
//			Transformer.transform(transform, exprTransform, op)
//			Query after = OpAsQuery.asQuery(afterOp);
//			QueryUtils.restoreQueryForm(after, before);

//			Transformer.transform(new TransformCopy(), op)
//			= OpAsQuery.asQu)

            //Query after = QueryTransformOps.transform(before, elform, exform);


            //QueryTransformOps.
//			QueryUtils.applyNodeTransform(query, nodeTransform)
            Query after = QueryUtils.applyNodeTransform(before, xform);
            result = new SparqlStmtQuery(after);
        } else if(stmt.isUpdateRequest()) {

            UpdateRequest before = stmt.getAsUpdateStmt().getUpdateRequest();
            UpdateRequest after = UpdateRequestUtils.copyTransform(before, update -> {
                // Transform UpdataData ourselves as
                // up to Jena 3.11.0 (inclusive) transforms do not affect UpdateData objects
                Update r = update instanceof UpdateData
                    ? UpdateUtils.copyWithQuadTransform((UpdateData)update, q -> QuadUtils.applyNodeTransform(q, xform))
                    : UpdateTransformOps.transform(update, elform, exform);
                return r;
            });

//			ElementTransform elform = new ElementTransformSubst2(xform);
//			UpdateRequest after = UpdateTransformOps.transform(before, elform, new ExprTransformNodeElement(xform, elform));
            result = new SparqlStmtUpdate(after);
        } else {
            result = stmt;
        }

        return result;
    }


    public static SparqlStmtIterator processFile(PrefixMapping pm, String filenameOrURI)
            throws FileNotFoundException, IOException {

        return processFile(pm, filenameOrURI, null);
    }


    public static URI extractBaseIri(String filenameOrURI) {
        Context context = null;
        StreamManager streamManager = StreamManager.get(context);

        // Code taken from jena's RDFParser
        String urlStr = streamManager.mapURI(filenameOrURI);

        URI uri;
        try {
            uri = new URI(urlStr);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
//		String result = parent.toString();
//		return result;
        return parent;
    }

    // TODO Move to utils or io - also, internally uses rdf content type for requests, which
    // is not what we want
    public static String loadString(String filenameOrURI) throws IOException {
        String result;
        try(InputStream in = openInputStream(filenameOrURI)) {
            result = in != null ? CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8)) : null;
        }

        return result;
    }


    // FIXME Can we remove this in favor of RDFDataMgr.open()?
    public static TypedInputStream openInputStream(String filenameOrURI) {
        Context context = null;

        StreamManager streamManager = StreamManager.get(context);

        // Code taken from jena's RDFParser
        String urlStr = streamManager.mapURI(filenameOrURI);
        TypedInputStream in;
        urlStr = StreamManager.get(context).mapURI(urlStr);
        if ( urlStr.startsWith("http://") || urlStr.startsWith("https://") ) {
            HttpClient httpClient = null;
            String acceptHeader =
                ( httpClient == null ) ? WebContent.defaultRDFAcceptHeader : null;
            in = HttpOp.httpGet(urlStr, acceptHeader);
        } else {
            in = streamManager.open(urlStr);
        }

        return in;
    }

    public static SparqlStmtIterator readStmts(String filenameOrURI, SparqlStmtParser parser)
            throws IOException {
        InputStream in = openInputStream(filenameOrURI);
        if(in == null) {
            throw new IOException("Could not open input stream from " + filenameOrURI);
        }

        SparqlStmtIterator result = parse(in, parser);
        return result;
    }

    /**
     *
     * @param pm A <b>modifiable<b> prefix mapping
     * @param filenameOrURI
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static SparqlStmtIterator processFile(PrefixMapping pm, String filenameOrURI, String baseIri)
            throws FileNotFoundException, IOException {

        InputStream in = openInputStream(filenameOrURI);
        if(in == null) {
            throw new IOException("Could not open input stream from " + filenameOrURI);
        }

        if(baseIri == null) {
            URI tmp = extractBaseIri(filenameOrURI);
            baseIri = tmp.toString();
//	        URI uri;
//			try {
//				uri = new URI(urlStr);
//			} catch (URISyntaxException e) {
//				throw new RuntimeException(e);
//			}
//	        URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
//			baseIri = parent.toString();
        }

        return processInputStream(pm, baseIri, in);
        //stmts.forEach(stmt -> process(conn, stmt, sink));
    }


    /**
     * Parse all queries from th given input stream using a sparql parser with
     * namespace tracking.
     *
     * This is just a convenience function which creates a sparql parser
     * from the arguments and calls {@link #parse(InputStream, Function)} - deprecate?
     */
    @Deprecated
    public static SparqlStmtIterator processInputStream(PrefixMapping pm, String baseIri, InputStream in)
            throws IOException {

//		File file = new File(filename).getAbsoluteFile();
//		if(!file.exists()) {
//			throw new FileNotFoundException(file.getAbsolutePath() + " does not exist");
//		}
//
//		String dirName = file.getParentFile().getAbsoluteFile().toURI().toString();

        Prologue prologue = new Prologue();
        //prologue.getPrefixMapping().setNsPrefixes(pm);
        prologue.setPrefixMapping(pm);

        prologue.setBaseURI(baseIri);

        Function<String, SparqlStmt> rawSparqlStmtParser = SparqlStmtParserImpl.create(Syntax.syntaxARQ,
                prologue, true);// .getQueryParser();


        // Wrap the parser with tracking the prefixes
        SparqlStmtParser sparqlStmtParser = SparqlStmtParser.wrapWithNamespaceTracking(pm, rawSparqlStmtParser);
        SparqlStmtIterator stmts = SparqlStmtUtils.parse(in, sparqlStmtParser);

        return stmts;
    }

    /**
     * Parse an input stream with the given parser.
     * Parsing happens on calling next/hasNext on the iterator so
     * this method will not raise a ParseException on invalid input.
     *
     * Because of the trial/error approach of the parser the whole input stream is
     * immediately read into a string and closed
     *
     * @param in
     * @param parser
     * @return
     * @throws IOException
     */
    public static SparqlStmtIterator parse(InputStream in, Function<String, SparqlStmt> parser)
            throws IOException {
        String str;
        try {
            str = CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
        } finally {
            in.close();
        }

        SparqlStmtIterator result = new SparqlStmtIterator(parser, str);
        return result;
    }


    public static SPARQLResultEx execAny(QueryExecution qe, Query q) {
        SPARQLResultEx result;

        if (q == null) {
            q = qe.getQuery();
        }

        if (q.isConstructQuad()) {
            Iterator<Quad> it = qe.execConstructQuads();
            result = SPARQLResultEx.createQuads(it, qe::close);

        } else if (q.isConstructType()) {
            // System.out.println(Algebra.compile(q));

            Iterator<Triple> it = qe.execConstructTriples();
            result = SPARQLResultEx.createTriples(it, qe::close);
        } else if (q.isSelectType()) {
            ResultSet rs = qe.execSelect();
            result = new SPARQLResultEx(rs, qe::close);
        } else if(q.isJsonType()) {
            Iterator<JsonObject> it = qe.execJsonItems();
            result = new SPARQLResultEx(it, qe::close);
        } else if (q.isAskType()) {
            boolean v = qe.execAsk();
            result = new SPARQLResultEx(v);
        } else if (q.isDescribeType()) {
            Iterator<Triple> it = qe.execDescribeTriples();
            result = SPARQLResultEx.createTriples(it, qe::close);
        } else {
            throw new RuntimeException("Unsupported query type");
        }

        return result;
    }

    public static SPARQLResultEx execAny(RDFConnection conn, SparqlStmt stmt, Consumer<Context> cxtMutator) {
        SPARQLResultEx result = null;

        if (stmt.isQuery()) {
            SparqlStmtQuery qs = stmt.getAsQueryStmt();
            Query q = qs.getQuery();

            if(q == null) {
                String queryStr = qs.getOriginalString();
                q = QueryFactory.create(queryStr, Syntax.syntaxARQ);
            }

            //conn.begin(ReadWrite.READ);
            // SELECT -> STDERR, CONSTRUCT -> STDOUT
            QueryExecution qe = conn.query(q);
            Context cxt = qe.getContext();
            if(cxt != null) {
                if (cxtMutator != null) {
                    cxtMutator.accept(cxt);
                }

                cxt.set(symConnection, conn);
            }

            result = execAny(qe, q);
        } else if (stmt.isUpdateRequest()) {
            UpdateRequest u = stmt.getAsUpdateStmt().getUpdateRequest();

            // conn.update(u);
//            Context cxt = ARQ.getContext().copy();
//            if (cxtMutator != null) {
//                cxtMutator.accept(cxt);
//            }
            UpdateExecution ue = conn.newUpdate().update(u).build();
//            if (ue.getContext() == null) {
//                System.err.println("Context of update request was null");
//            }
            if (cxtMutator != null) {
                Context cxt = ue.getContext();

                if (cxt != null) {
                    cxtMutator.accept(cxt);
                } else {
                    // XXX Perhaps better log a warning if the cxt is null and cxtMutator is given?
                }
            }

            ue.execute();
            // .context(cxt).execute();

            result = SPARQLResultEx.createUpdateType();
        }

        return result;
    }

    /** In-place update. */
    public static void overwriteDatasetDescription(SparqlStmt stmt, DatasetDescription dd) {
        if (stmt.isParsed()) {
            if (stmt.isQuery()) {
                QueryUtils.overwriteDatasetDescription(stmt.getQuery(), dd);
            } else if (stmt.isUpdateRequest()) {
                UpdateRequestUtils.overwriteDatasetDescription(stmt.getUpdateRequest(), dd);
            }
        } else {
            throw new IllegalArgumentException("Cannot apply dataset description to a SPARQL query that has not been parsed.");
        }
    }

    /**
     * Create a sink that for line based format
     * streams directly to the output stream or collects quads in memory and emits them
     * all at once in the given format when flushing the sink.
     *
     * @param r
     * @param format
     * @param out
     * @param dataset The dataset implementation to use for non-streaming data.
     *                Allows for use of insert-order preserving dataset implementations.
     * @return
     */
//    public static Sink<Quad> createSinkQuads(RDFFormat format, OutputStream out, PrefixMapping pm, Supplier<Dataset> datasetSupp) {
//        boolean useStreaming = format == null ||
//                Arrays.asList(Lang.NTRIPLES, Lang.NQUADS).contains(format.getLang());
//
//        Sink<Quad> result;
//        if(useStreaming) {
//        	StreamRDF s = StreamRDFLib.writer(out);
//            result = new SinkQuadOutput(out, null, null);
//        } else {
//            Dataset dataset = datasetSupp.get();
//            SinkQuadsToDataset core = new SinkQuadsToDataset(false, dataset.asDatasetGraph());
//
//            return new Sink<Quad>() {
//                @Override
//                public void close() {
//                    core.close();
//                }
//
//                @Override
//                public void send(Quad item) {
//                    core.send(item);
//                }
//
//                @Override
//                public void flush() {
//                    core.flush();
//
//                    // TODO Prefixed graph names may break
//                    // (where to define their namespace anyway? - e.g. in the default or the named graph?)
//                    PrefixMapping usedPrefixes = new PrefixMappingImpl();
//
//                    Stream.concat(
//                            Stream.of(dataset.getDefaultModel()),
//                            Streams.stream(dataset.listNames()).map(dataset::getNamedModel))
//                    .forEach(m -> {
//                        // PrefixMapping usedPrefixes = new PrefixMappingImpl();
//                        try(Stream<Node> nodeStream = GraphUtils.streamNodes(m.getGraph())) {
//                            PrefixUtils.usedPrefixes(pm, nodeStream, usedPrefixes);
//                        }
//                        m.clearNsPrefixMap();
//                        // m.setNsPrefixes(usedPrefixes);
//                    });
//
//                    dataset.getDefaultModel().setNsPrefixes(usedPrefixes);
//                    RDFDataMgr.write(out, dataset, format);
//                }
//            };
//        }
//
//        return result;
//    }

    public static void output(
            SPARQLResultEx rr,
            SPARQLResultVisitor sink) {
        try(SPARQLResultEx r = rr) {
            SPARQLResultVisitor.forward(r, sink);
        } catch (Exception e) {
            throw HttpExceptionUtils.makeHumanFriendly(e);
        }
    }

//    public static void output(
//        SPARQLResultEx rr,
//        Consumer<Quad> sink
//    ) {
//        SPARQLResultVisitor tmp = new SPARQLResultSinkQuads(sink);
//        output(rr, tmp);
//    }

//    public static void output(SPARQLResultEx r) {
//        SinkQuadOutput dataSink = new SinkQuadOutput(System.out, null, null);
//        try {
//            output(r, dataSink::send);
//        } finally {
//            dataSink.flush();
//            dataSink.close();
//        }
//    }

//	public static void output(SPARQLResultEx r) {
//		//logger.info("Processing SPARQL Statement: " + stmt);
//		if (r.isQuads()) {
//			SinkQuadOutput sink = new SinkQuadOutput(System.out, null, null);
//			Iterator<Quad> it = r.getQuads();
//			while (it.hasNext()) {
//				Quad t = it.next();
//				sink.send(t);
//			}
//			sink.flush();
//			sink.close();
//
//		} else if (r.isTriples()) {
//			// System.out.println(Algebra.compile(q));
//
//			SinkTripleOutput sink = new SinkTripleOutput(System.out, null, null);
//			Iterator<Triple> it = r.getTriples();
//			while (it.hasNext()) {
//				Triple t = it.next();
//				sink.send(t);
//			}
//			sink.flush();
//			sink.close();
//		} else if (r.isResultSet()) {
//			ResultSet rs =r.getResultSet();
//			String str = ResultSetFormatter.asText(rs);
//			System.err.println(str);
//		} else if(r.isJson()) {
//			JsonArray tmp = new JsonArray();
//			r.getJsonItems().forEachRemaining(tmp::add);
//			String json = tmp.toString();
//			System.out.println(json);
//		} else {
//			throw new RuntimeException("Unsupported query type");
//		}
//	}

    public static void process(RDFConnection conn, SparqlStmt stmt, Consumer<Context> cxtMutator, SPARQLResultVisitor sink) {
        SPARQLResultEx sr = execAny(conn, stmt, cxtMutator);
        output(sr, sink);
    }


//    public static void processOld(RDFConnection conn, SparqlStmt stmt) {
//        //logger.info("Processing SPARQL Statement: " + stmt);
//
//        if (stmt.isQuery()) {
//            SparqlStmtQuery qs = stmt.getAsQueryStmt();
//            Query q = qs.getQuery();
//            q.isConstructType();
//            conn.begin(ReadWrite.READ);
//            // SELECT -> STDERR, CONSTRUCT -> STDOUT
//            QueryExecution qe = conn.query(q);
//
//            if (q.isConstructQuad()) {
//                // ResultSetFormatter.ntrqe.execConstructTriples();
//                //throw new RuntimeException("not supported yet");
//                SinkQuadOutput sink = new SinkQuadOutput(System.out, null, null);
//                Iterator<Quad> it = qe.execConstructQuads();
//                while (it.hasNext()) {
//                    Quad t = it.next();
//                    sink.send(t);
//                }
//                sink.flush();
//                sink.close();
//
//            } else if (q.isConstructType()) {
//                // System.out.println(Algebra.compile(q));
//
//                SinkTripleOutput sink = new SinkTripleOutput(System.out, null, null);
//                Iterator<Triple> it = qe.execConstructTriples();
//                while (it.hasNext()) {
//                    Triple t = it.next();
//                    sink.send(t);
//                }
//                sink.flush();
//                sink.close();
//            } else if (q.isSelectType()) {
//                ResultSet rs = qe.execSelect();
//                String str = ResultSetFormatter.asText(rs);
//                System.err.println(str);
//            } else if(q.isJsonType()) {
//                String json = qe.execJson().toString();
//                System.out.println(json);
//            } else {
//                throw new RuntimeException("Unsupported query type");
//            }
//
//            conn.end();
//        } else if (stmt.isUpdateRequest()) {
//            UpdateRequest u = stmt.getAsUpdateStmt().getUpdateRequest();
//
//            conn.update(u);
//        }
//    }

    public static Op toAlgebra(SparqlStmt stmt) {
        Op result = null;

        if(stmt.isQuery()) {
            Query q = stmt.getAsQueryStmt().getQuery();
            result = Algebra.compile(q);
        } else if(stmt.isUpdateRequest()) {
            UpdateRequest ur = stmt.getAsUpdateStmt().getUpdateRequest();
            for(Update u : ur) {
                if(u instanceof UpdateModify) {
                    Element e = ((UpdateModify)u).getWherePattern();
                    result = Algebra.compile(e);
                }
            }
        }

        return result;
    }


    /**
     * Get all variables mentioned in the nodes w.r.t. to the variable representations
     * supported by {@link NodeEnvsubst}
     *
     * @param sparqlStmts
     * @return
     */
    public static Set<String> getMentionedEnvVars(Collection<? extends SparqlStmt> sparqlStmts) {
        NodeTransformCollectNodes collector = new NodeTransformCollectNodes();

        for (SparqlStmt sparqlStmt : sparqlStmts) {
            SparqlStmtUtils.applyNodeTransform(sparqlStmt, collector);
        }

        // Get all environment references
        // TODO Make this a util function
        Set<String> usedEnvVarNames = collector.getNodes().stream()
            .map(NodeEnvsubst::getEnvKey)
            .filter(Objects::nonNull)
            .map(Entry::getKey)
            .distinct()
            .collect(Collectors.toSet());

        return usedEnvVarNames;
    }

}

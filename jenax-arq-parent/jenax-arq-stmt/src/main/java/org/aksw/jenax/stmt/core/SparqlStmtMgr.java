package org.aksw.jenax.stmt.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jenax.arq.util.node.NodeEnvsubst;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.stmt.resultset.SPARQLResultSinkQuads;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sparql.util.ModelUtils;

public class SparqlStmtMgr {
    // private static final Logger logger = LoggerFactory.getLogger(SparqlStmtMgr.class);

    public static void readDataset(Dataset dataset, String filenameOrURI, Consumer<Quad> quadConsumer) {
        RDFConnection conn = RDFConnectionFactory.connect(dataset);
        readConnection(conn, filenameOrURI, quadConsumer);
    }

    public static void readModel(Model model, String filenameOrURI, Consumer<Quad> quadConsumer) {
        Dataset dataset = DatasetFactory.wrap(model);
        readDataset(dataset, filenameOrURI, quadConsumer);
    }

    public static void readConnection(RDFConnection conn, String filenameOrURI, Consumer<Quad> quadConsumer) {
        readConnection(conn, filenameOrURI, quadConsumer, System::getenv);
    }

    /**
     * Execute a sequence of SPARQL update statements from a file against a model.
     * For example, can be used to materialize triples.
     *
     * @param model
     * @param filenameOrURI
     */
    public static void execSparql(Model model, String filenameOrURI) {
        execSparql(model, filenameOrURI, (Function<String, String>)null);
    }

    public static void execSparql(Model model, String filenameOrURI, Function<String, String> envLookup) {
        try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.wrap(model))) {
            execSparql(conn, filenameOrURI, envLookup);
        }
    }

    public static void execSparql(Model model, String filenameOrURI, Map<String, String> envMap) {
        try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.wrap(model))) {
            execSparql(conn, filenameOrURI, envMap == null ? null : envMap::get);
        }
    }

    public static void execSparql(RDFConnection conn, String filenameOrURI) {
        readConnection(conn, filenameOrURI, null);
    }

    public static void execSparql(RDFConnection conn, String filenameOrURI, Function<String, String> envLookup) {
        readConnection(conn, filenameOrURI, null, envLookup);
    }


    /**
     * Load a single query from a given file, URL or classpath resource
     *
     * @param filenameOrURI
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException
     */
    public static Query loadQuery(String filenameOrURI) {
        return loadQuery(filenameOrURI, DefaultPrefixes.get());
    }

//	public static List<Query> loadQueries(String filenameOrURI, PrefixMapping pm) throws FileNotFoundException, IOException, ParseException {
//		List<SparqlStmt> stmts = Streams.stream(SparqlStmtUtils.processFile(pm, filenameOrURI))
//				.collect(Collectors.toList());
//
//		Query result;
//		if(stmts.size() == 1) {
//			result = stmts.iterator().next().getQuery();
//			result.setBaseURI((String)null);
//			QueryUtils.optimizePrefixes(result);
//		} else {
//			throw new RuntimeException("Expected a single query in " + filenameOrURI + "; got " + stmts.size());
//		}
//
//
//
//		return result;
//	}

    public static List<Query> loadQueries(InputStream in, PrefixMapping pm) throws FileNotFoundException, IOException, ParseException {
        List<SparqlStmt> stmts = Streams.stream(SparqlStmtUtils.processInputStream(pm, null, in))
                .collect(Collectors.toList());

        List<Query> result = new ArrayList<>();
        for(SparqlStmt stmt : stmts) {
            Query query = stmt.getQuery();
            query.setBaseURI((String)null);
            QueryUtils.optimizePrefixes(query);
            result.add(query);
        }

        return result;
    }

    public static List<Query> loadQueries(String filenameOrURI, PrefixMapping pm) throws FileNotFoundException, IOException, ParseException {
        List<SparqlStmt> stmts = Streams.stream(SparqlStmtUtils.processFile(pm, filenameOrURI))
                .collect(Collectors.toList());

        List<Query> result = new ArrayList<>();
        for(SparqlStmt stmt : stmts) {
            Query query = stmt.getQuery();
            query.setBaseURI((String)null);
            QueryUtils.optimizePrefixes(query);
            result.add(query);
        }

        return result;
    }

    /**
     * Load exactly a single query from a file or URI.
     * Search includes the classpath.
     *
     * @param filenameOrURI
     * @param pm Prefix mapping
     * @return Exactly a single query - nevel null.
     */
    public static Query loadQuery(String filenameOrURI, PrefixMapping pm) {
        List<Query> queries;
        try {
            queries = loadQueries(filenameOrURI, pm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(queries.size() != 1) {
            throw new RuntimeException("Expected a single query in " + filenameOrURI + "; got " + queries.size());
        }

        Query result = queries.get(0);
        return result;
    }

    public static void readConnection(RDFConnection conn, String filenameOrURI, Consumer<Quad> quadConsumer, Function<String, String> envLookup) {
        //Sink<Quad> sink = SparqlStmtUtils.createSink(outFormat, System.out);

        PrefixMapping pm = new PrefixMappingImpl();
//		pm.setNsPrefixes(PrefixMapping.Extended);
//		JenaExtensionUtil.addPrefixes(pm);
//
//		JenaExtensionHttp.addPrefixes(pm);
//
//		// Extended SERVICE <> keyword implementation
//		JenaExtensionFs.registerFileServiceHandler();
        pm.setNsPrefixes(DefaultPrefixes.get());

        try {
            List<SparqlStmt> stmts = Streams.stream(SparqlStmtUtils.processFile(pm, filenameOrURI))
                    .collect(Collectors.toList());

            for(SparqlStmt stmt : stmts) {
                SparqlStmt stmt2 = envLookup == null
                    ? stmt
                    : SparqlStmtUtils.applyNodeTransform(stmt, x -> NodeEnvsubst.subst(x, envLookup));
                SparqlStmtUtils.process(conn, stmt2, new SPARQLResultSinkQuads(quadConsumer));
            }

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a model by concatenation of a series of construct queries from a given .sparql file
     * @param conn
     * @param filenameOrURI
     * @return
     */
    public static Model execConstruct(RDFConnection conn, String filenameOrURI) {
        Model result = ModelFactory.createDefaultModel();
        readConnection(conn, filenameOrURI,
                q -> result.add(ModelUtils.tripleToStatement(result, q.asTriple())));
        return result;
    }

    /**
     * Read a sequence of SPARQL statements from either a file or string.
     * Hence, probing of the 'filenameOrStr' argument is performed.
     *
     * @param globalPrefixes
     * @param sparqlParser
     * @param actualConn
     * @param baseIri
     * @param filenameOrStr
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static Iterator<SparqlStmt> loadSparqlStmts(
            String filenameOrStr,
            // PrefixMapping globalPrefixes,
            SparqlStmtParser sparqlParser) throws ParseException, IOException {

        // Check whether the argument is an inline sparql statement
        Iterator<SparqlStmt> it = null;
        try {
//            it = SparqlStmtUtils.processFile(sparqlParser, filenameOrStr, baseIri);
            it = SparqlStmtUtils.readStmts(filenameOrStr, sparqlParser);
        } catch(IOException e) {

            try {
                SparqlStmt sparqlStmt = sparqlParser.apply(filenameOrStr);
                it = Collections.singletonList(sparqlStmt).iterator();
            } catch(ARQException f) {
                // Possibly not a sparql query
                Throwable c = f.getCause();
                if(c instanceof QueryParseException) {
                    QueryParseException qpe = (QueryParseException)c;
                    boolean mentionsEncounteredSlash = Optional.ofNullable(qpe.getMessage())
                            .orElse("").contains("Encountered: \"/\"");

                    // Note: Jena throws query parse exceptions for certain semantic issues, such as if the
                    // same variable is assigned to in two different BINDs.
                    // For this reason parse errors can occur at invalid char positions such as
                    // line=-1 and/or column=-1 it
                    // qpe.getLine() > 1 ||
                    // && qpe.getColumn() > 1)
                    if  (!mentionsEncounteredSlash) {
                        throw new RuntimeException(filenameOrStr + " could not be openend and failed to parse as SPARQL query", f);
                    }
                }

                throw new IOException("Could not open " + filenameOrStr, e);
            }
        }

        return it;
    }


    /**
     * Load a query of which one variable acts as a placeholder as a function.
     * This method may be refactored to use a {@link ParameterizedSparqlString} as a base.
     */
    public static Function<String, Query> loadTemplate(String fileOrURI, String templateArgName) throws FileNotFoundException, IOException, ParseException {
        Query templateQuery = SparqlStmtMgr.loadQuery(fileOrURI);

        Function<String, Query> result = value -> {
            Map<String, String> map = Collections.singletonMap(templateArgName, value);
            Query r = QueryUtils.applyNodeTransform(templateQuery, x -> NodeEnvsubst.subst(x, map::get));
            return r;
        };
        return result;
    };

//	public static Model execConstruct(RDFConnection conn, String queryStr) {
//		Model result = ModelFactory.createDefaultModel();
//		readConnection(conn, filenameOrURI,
//				q -> result.add(ModelUtils.tripleToStatement(result, q.asTriple())));
//		return result;
//	}

//    public static void main(String[] args) {
//        Model model = ModelFactory.createDefaultModel();
//        readModel(model, "sparql-test.sparql", null);
//
//        System.out.println("Model size: " + model.size());
//
//    }
}

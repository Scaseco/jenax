package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.aksw.commons.io.binseach.BinarySearchOnSortedFile;
import org.aksw.commons.io.binseach.BinarySearcher;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jenax.arq.util.graph.GraphFindRaw;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrRx;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.lang.IteratorParsers;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

/**
 * Graph implementation against a binary searcher for subjects.
 */
public class GraphFromPrefixMatcher
    extends GraphBase
    implements GraphFindRaw
{
    private static final Logger logger = LoggerFactory.getLogger(GraphFromPrefixMatcher.class);

    protected BinarySearcher binarySearcher;

    public GraphFromPrefixMatcher(BinarySearcher binarySearcher) {
        super();
        this.binarySearcher = binarySearcher;
    }

    public static Iterator<Triple> createBaseIterator(BinarySearcher binarySearcher, Triple triplePattern) {
        String prefix = derivePrefix(triplePattern);

        Iterator<Triple> result;
        if (prefix == null) {
            result = Collections.emptyIterator();
        } else {
            InputStream in;
            try {
                in = binarySearcher.search(prefix);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            result = Iter.onCloseIO(IteratorParsers.createIteratorNTriples(in, RDFDataMgrRx.dftProfile()), in::close);
        }

        if (false) {
            if ("".equals(prefix)) {
                System.err.println("WARN: Ungrounded subject: " + triplePattern);
            } else {
                System.err.println(Thread.currentThread().getName() + ": Lookup with: " + triplePattern);
            }
        }

        if(false) {
            if(prefix != null && prefix.length() > 0) {
                List<Triple> t = Iter.toList(result);
                System.out.println("For prefix " + prefix + " got " + t.size() + " triples");
                result = t.iterator();
            } else {
                System.out.println("Got pattern: " + triplePattern);
            }
        }

        return result;
    }

    @Override
    public ExtendedIterator<Triple> findRaw(Triple triplePattern) {
        return WrappedIterator.create(createBaseIterator(binarySearcher, triplePattern));
    }

    protected ExtendedIterator<Triple> graphBaseFindCore(Triple triplePattern) throws IOException {
        Iterator<Triple> it = Iter.filter(createBaseIterator(binarySearcher, triplePattern),
                triplePattern::matches);
        ExtendedIterator<Triple> result = WrappedIterator.create(it);
        return result;
    }

    public static String derivePrefix(Triple t) {
        String result = derivePrefix(t.getSubject());
        if (result != null) {
            String tmp = derivePrefix(t.getPredicate());
            if (tmp != null) {
                result += " " + tmp;
            }
        }
        return result;
    }

    public static String derivePrefix(Node s) {
        // Construct the prefix from the subject
        // Because whitespaces between subject and predicate may differ, do not include
        // further components
        String prefix;
        if(s.equals(Node.ANY) || s.isVariable()) {
            prefix = "";
        } else if (s.isBlank()) {
            prefix = "_:" + s.getBlankNodeLabel();
        } else if (s.isURI() ){
            prefix = "<" + s.getURI() + ">";
        } else {
            // Literal in subject position - skip
            prefix = null;
        }
        return prefix;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
        ExtendedIterator<Triple> result;
        try {
            result = graphBaseFindCore(triplePattern);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void close() {
        try {
            binarySearcher.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {

        JenaSystem.init();
        Function<String, SparqlStmt> parser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, DefaultPrefixes.get(), false);

//		Path path = Paths.get("/home/raven/Projects/Data/LSQ/wtf100k.nt");
        Path path = Paths.get("/home/raven/Projects/Data/LSQ/deleteme.sorted.nt");

//		Path path = Paths.get("/home/raven/Projects/Data/LSQ/wtf.sorted.nt");
        Graph graph = new GraphFromPrefixMatcher(BinarySearchOnSortedFile.create(path));

        Model m = ModelFactory.createModelForGraph(graph);

        String queryStr;

        Iterator<String> itSubject = Files.lines(Paths.get("/home/raven/Projects/Data/LSQ/subjects.shuffled.txt")).iterator();

        Stopwatch stopwatch = Stopwatch.createStarted();
        int i = 0;
        while(itSubject.hasNext()) {
            if(i % 100 == 0) {
                System.out.println(i);
            }
            ++i;

            String s = itSubject.next();
            queryStr = "SELECT * { " + s + " ?p ?o }";
            //System.out.println(queryStr);
            try(QueryExecution qe = QueryExecutionFactory.create(queryStr, m)) {
                long numResults = ResultSetFormatter.consume(qe.execSelect());
                if(numResults > 0) {
                    int xxx = 5;
                }
                //System.out.println("Num results: " + numResults);
                //System.out.println(ResultSetFormatter.asText(qe.execSelect()));
            }
        }
        System.out.println("Processed items in " + (stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) * 0.001) + " seconds");

        queryStr = parser.apply("SELECT *\n" +
                "           {\n" +
                "             # TODO Allocate some URI based on the dataset id\n" +
                "             BIND(BNODE() AS ?report)\n" +
                "             { SELECT ?p (COUNT(*) AS ?numTriples) (COUNT(DISTINCT ?s) AS ?numUniqS) (COUNT(DISTINCT ?o) AS ?numUniqO) {\n" +
                "               ?s ?p ?o\n" +
                "             } GROUP BY ?p }\n" +
                "           }").toString();

//		queryStr = "SELECT * { ?s ?p ?o . ?o ?x ?y . ?y ?a ?b } LIMIT 100 OFFSET 100000";
//		queryStr = "SELECT * { <http://lsq.aksw.org/res/swdf> ?p ?o } LIMIT 10";

//		Stopwatch stopwatch = Stopwatch.createStarted();
//		try(QueryExecution qe = QueryExecutionFactory.create(queryStr, m)) {
//			System.out.println(ResultSetFormatter.asText(qe.execSelect()));
//		}
//	    System.out.println("Processed items in " + (stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) * 0.001) + " seconds");



        //Set<Resource> res = Streams.stream(m.listSubjects()).limit(1000).collect(Collectors.toSet());
//
//		Collection<Resource> res = Arrays.asList(
//				m.createResource("http://lsq.aksw.org/res/re-swdf-q-6ffab076-8ab0c230a859bbe3-02014-52-27_03:52:43")
//		);
//
//		for(Resource r: res) {
//			System.out.println(r);
//			for(Statement stmt : r.listProperties().toList()) {
//				System.out.println("  " + stmt);
//			}
////			System.out.println(r + ": " + r.listProperties().toSet().size());
//		}

    }
}

package org.aksw.jena_sparql_api.core.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.mapper.BindingMapperUtils;
import org.aksw.jenax.arq.aggregation.BindingMapper;
import org.aksw.jenax.arq.aggregation.BindingMapperQuad;
import org.aksw.jenax.arq.util.binding.TableUtils;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.execution.factory.query.QueryExecutionFactory;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetCloseable;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.expr.aggregate.AggCountDistinct;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;


public class QueryExecutionUtils {
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionUtils.class);

    public static final Var vg = Var.alloc("g");
    public static final Var vs = Var.alloc("s");
    public static final Var vp = Var.alloc("p");
    public static final Var vo = Var.alloc("o");


    public static Table execSelectTable(Supplier<QueryExecution> qeSupp) {
        Table result;
        try (QueryExecution qe = qeSupp.get()) {
            ResultSet rs = qe.execSelect();
            result = TableUtils.createTable(rs);
        }
        return result;
    }


    public static void abortAfterFirstRow(QueryExecution qe) {
        Query query = qe.getQuery();
        assert query != null : "QueryExecution did not tell us which query it is bound to - query was null";
        int queryType = query.getQueryType();

        try {
            switch (queryType) {
            case Query.QueryTypeAsk:
                qe.execAsk();
                break;
            case Query.QueryTypeConstruct:
                Iterator<Triple> itC = qe.execConstructTriples();
                itC.hasNext();
                break;
            case Query.QueryTypeDescribe:
                Iterator<Triple> itD = qe.execDescribeTriples();
                itD.hasNext();
                break;
            case Query.QueryTypeSelect:
                ResultSet rs = qe.execSelect();
                rs.hasNext();
                break;
            default:
                throw new RuntimeException("Unknown query type - should not happen: queryType = " + queryType);
            }
        } finally {
            qe.abort();
        }
    }

    /**
     * Consumes the full response (result set or triples) of a QueryExecution
     * Only uses the iterator-based methods to avoid cluttering up the heap
     *
     * @param qe
     */
    public static long consume(QueryExecution qe) {
        Query query = qe.getQuery();
        assert query != null : "QueryExecution did not tell us which query it is bound to - query was null";
        int queryType = query.getQueryType();

        long result;
        switch (queryType) {
        case Query.QueryTypeAsk:
            qe.execAsk();
            result = 1;
            break;
        case Query.QueryTypeConstruct:
            Iterator<Triple> itC = qe.execConstructTriples();
            result = Iterators.size(itC);
            break;
        case Query.QueryTypeDescribe:
            Iterator<Triple> itD = qe.execDescribeTriples();
            result = Iterators.size(itD);
            break;
        case Query.QueryTypeSelect:
            ResultSet rs = qe.execSelect();
            result = ResultSetFormatter.consume(rs);
            break;
        default:
            throw new RuntimeException("Unknown query type - should not happen: queryType = " + queryType);
        }

        return result;
    }

    public static Iterator<Quad> findQuads(QueryExecutionFactory qef, Node g, Node s, Node p, Node o) {
        Quad quad = new Quad(g, s, p, o);
        Query query = QueryGenerationUtils.createQueryQuad(new Quad(g, s, p, o));
        BindingMapper<Quad> mapper = new BindingMapperQuad(quad);
        Iterator<Quad> result = BindingMapperUtils.execMapped(qef, query, mapper);
        return result;
    }

    public static void tryClose(Object obj) {
        if(obj instanceof AutoCloseable) {
            try {
                ((AutoCloseable) obj).close();
            } catch (Exception e) {
                logger.warn("Exception while closing", e);
            }
        } else if(obj instanceof Closeable) {
            ((Closeable) obj).close();
        }
    }

    /**
     * Exec construct with wrapper to extended iterator
     * @param qef
     * @param query
     * @return
     */
    public static ExtendedIterator<Triple> execConstruct(QueryExecutionFactory qef, Query query) {
        QueryExecution qe = qef.createQueryExecution(query);

        return execConstruct(qe, query);
    }

    public static ExtendedIterator<Triple> execConstruct(QueryExecution qe, Query query) {
        Iterator<Triple> it = qe.execConstructTriples();

        ExtendedIterator<Triple> result = WrappedIterator.create(Iter.onClose(it, () -> { tryClose(it); qe.close(); }));
        return result;
    }

    public static boolean validate(QueryExecutionFactory qef, boolean suppressException) {

        boolean result;
        try {
            Query query = QueryFactory.create("SELECT * { ?s a ?t } Limit 1");
            QueryExecution qe = qef.createQueryExecution(query);
            ResultSet rs = qe.execSelect();
            ResultSetFormatter.consume(rs);
            result = true;
        } catch(Exception e) {
            if(!suppressException) {
                throw new RuntimeException(e);
            }
            result = false;
        }

        return result;
    }


    public static Iterator<Quad> createIteratorDumpQuads(QueryExecutionFactory qef) {
        String queryStr = "Select ?g ?s ?p ?o { Graph ?g { ?s ?p ?o } }";
        final QueryExecution qe = qef.createQueryExecution(queryStr);
        ResultSet tmp = qe.execSelect();

        ResultSetCloseable rs = new ResultSetCloseable(tmp, qe);

        Iterator<Quad> result = new IteratorNQuads(rs);
        return result;
    }

    public static void createDumpNQuads(QueryExecutionFactory qef, Sink<Quad> sink) {
        Iterator<Quad> it = createIteratorDumpQuads(qef);
        while(it.hasNext()) {
            Quad quad = it.next();
            sink.send(quad);
        }
    }

    public static Set<Quad> createDumpNQuads(QueryExecutionFactory qef) {
        SinkQuadsToCollection<? extends Set<Quad>> sink = SinkQuadsToCollection.createSinkHashSet();
        createDumpNQuads(qef, sink);
        Set<Quad> result = sink.getQuads();
        return result;
    }



    public static Iterator<Triple> createIteratorDumpTriples(QueryExecutionFactory qef) {
        //Query query = CannedQueryUtils.spoTemplate();
        String queryStr = "Construct { ?s ?p ?o } { ?s ?p ?o }";
        QueryExecution qe = qef.createQueryExecution(queryStr);
        Iterator<Triple> result = qe.execConstructTriples();
        return result;
    }


    public static long fetchBindingCount(String serviceUrl, Query query) {
        long result;
        try (QueryExecution qe = QueryExecutionHTTP.newBuilder().endpoint(serviceUrl).query(query).build()) {
            result = ResultSetFormatter.consume(qe.execSelect());
        }
        return result;
    }

    // TODO Consolidate with QueryGenerationUtils
    public static long countQuery(Query query, QueryExecutionFactory qef) {
        boolean needsWrapping = !query.getGroupBy().isEmpty() || !query.getAggregators().isEmpty();

        boolean useCountDistinct = !needsWrapping && query.isDistinct() && query.isQueryResultStar();

        //boolean isDistinct = query.isDistinct();


        Aggregator agg = useCountDistinct
                ? new AggCountDistinct()
                : new AggCount();

        Query cQuery = new Query();
        cQuery.setQuerySelectType();
        cQuery.setPrefixMapping(query.getPrefixMapping());
        cQuery.getProject().add(Vars.c, new ExprAggregator(Vars.x, agg));

        Element queryPattern;
        if(needsWrapping) {
            Query q = query.cloneQuery();
            q.setPrefixMapping(new PrefixMappingImpl());
            queryPattern = new ElementSubQuery(q);
        } else {
            queryPattern = query.getQueryPattern();
        }


        cQuery.setQueryPattern(queryPattern);
//System.out.println("CQUERY: " + cQuery);
        QueryExecution qe = qef.createQueryExecution(cQuery);
        long result = ServiceUtils.fetchInteger(qe, Vars.c);

        return result;
    }

    @Deprecated // Remove once countQuery works as espected
    public static long countQueryOld(Query query, QueryExecutionFactory qef) {
        Var outputVar = Var.alloc("_c_");

        if(query.isConstructType()) {

            Element element = query.getQueryPattern();
            query = new Query();
            query.setQuerySelectType();
            query.setQueryResultStar(true);
            query.setQueryPattern(element);
        }

        Query countQuery = QueryFactory.create("Select (Count(*) As ?_c_) { {" + query + "} }", Syntax.syntaxSPARQL_11);


        QueryExecution qe = qef.createQueryExecution(countQuery);
        ResultSet rs = qe.execSelect();
        Binding binding = rs.nextBinding();
        Node node = binding.get(outputVar);
        Number numeric = (Number)node.getLiteralValue();
        long result = numeric.longValue();


        return result;
    }

    public static Var extractProjectVar(Query query) {
        List<Var> vars = query.getProjectVars();
        if(vars.size() != 1) {
            throw new RuntimeException("Exactly 1 var expected");
        }

        Var result = vars.get(0);

        return result;
    }

    public static Node executeSingle(QueryExecutionFactory qef, Query query) {
        Var var = extractProjectVar(query);

        Node result = executeSingle(qef, query, var);
        return result;
    }

    public static Node executeSingle(QueryExecutionFactory qef, Query query, Var var) {
        Node result = null;

        try(QueryExecution qe = qef.createQueryExecution(query)) {
            ResultSet rs = qe.execSelect();

            if(rs.hasNext()) {
                Binding binding = rs.nextBinding();
                result = binding.get(var);
            }

            if(rs.hasNext()) {
                logger.warn("A single result was retrieved, but more results exist - is this intended?");
            }
        }
        return result;
    }


    public static List<Node> executeList(QueryExecutionFactory qef, Query query) {
        Var var = extractProjectVar(query);

        List<Node> result = executeList(qef, query, var);
        return result;
    }


    public static List<Node> executeList(QueryExecutionFactory qef, Query query, Var var) {
        List<Node> result = new ArrayList<Node>();

        try(QueryExecution qe = qef.createQueryExecution(query)) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()) {
                //QuerySolutiors.next()
                Binding binding = rs.nextBinding();
                Node node = binding.get(var);

                result.add(node);
            }
        }

        return result;
    }

//    public static Iterator<Node> executeIterator(QueryExecutionFactory qef, Query query) {
//        Var var = extractProjectVar(query);
//
//        Iterator<Node> result = executeIterator(qef, query, var);
//        return result;
//    }
//
//
//    /**
//     * Warning: the iterator must be consumed, otherwise there will be a resource leak!!!
//     *
//     * @param qef
//     * @param query
//     * @param var
//     * @return
//     */
//    public static Iterator<Node> executeIterator(QueryExecutionFactory qef, Query query, Var var) {
//        QueryExecution qe = qef.createQueryExecution(query);
//        ResultSet rs = qe.execSelect();
//
//        Iterator<Binding> itBinding = new IteratorResultSetBinding(rs);
//        Function<Binding, Node> fn = FunctionBindingMapper.create(new BindingMapperProjectVar(var));
//
//        Iterator<Node> result = Iterators.transform(itBinding, new GuavaFunctionWrapper<>(fn));
//
//        return result;
//    }

}

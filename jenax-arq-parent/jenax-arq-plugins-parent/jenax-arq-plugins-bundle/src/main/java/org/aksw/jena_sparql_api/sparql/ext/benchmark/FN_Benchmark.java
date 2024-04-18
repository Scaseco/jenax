package org.aksw.jena_sparql_api.sparql.ext.benchmark;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.aksw.commons.util.stack_trace.StackTraceUtils;
import org.aksw.jena_sparql_api.sparql.ext.json.JenaJsonUtils;
import org.aksw.jenax.arq.util.exec.query.QueryExecUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.opencensus.common.Function;

/**
 * Function for benchmarking a sparql query on a given endpoint and obtain
 * the statistics as an RDF literal holding a JSON object.
 *
 * This function is similar to a service clause but it only produces benchmark result json object.
 * In fact, internally, the benchmark request is executed as a SERVICE call.
 *
 * Example:
 * <pre>
 * BIND(sys:benchmark(<http://dbpedia.org/sparql>, "SELECT * { ?s ?p ?o }" AS ?json)
 *
 * Lexical value of ?json:
 * {
 *   "size": 10000,
 *   "time": 0.5 // in seconds
 * }
 * </pre>
 */
public class FN_Benchmark
    extends FunctionBase
{
    private static final Logger logger = LoggerFactory.getLogger(FN_Benchmark.class);

    /** Called by super.{@link #exec(List, FunctionEnv)} */
//    @Override
//    public NodeValue exec(NodeValue v1, NodeValue v2) {
//        return null;
//    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        if (args.size() != 2) {
            throw new QueryBuildException("Function '"+Lib.className(this)+"' takes two arguments") ;
        }
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        throw new IllegalStateException("This method should never be called");
    }

    @Override
    protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
        // Only when base class is FunctionBaseX - Reuse base class for validation but don't do anything
        // super.exec(args, env);

        NodeValue serviceNv = args.get(0);
        NodeValue queryNv = args.get(1);

        if (serviceNv == null) {
            throw new VariableNotBoundException("Service target for benchmarking was not bound");
        }

        if (queryNv == null) {
            throw new VariableNotBoundException("The query for benchmark was not bound");
        }

        Node serviceNode = serviceNv.asNode();
        Node queryNode = queryNv.getNode();

        String queryStr = queryNode.getLiteralLexicalForm();
        Query query = QueryFactory.create(queryStr);
        query.setResultVars();
        Op op = Algebra.compile(query);
        OpService opService = new OpService(serviceNode, op, true);
        List<String> vars = query.getResultVars();
        JsonObject json = benchmarkResultSet(
                () -> QueryExecUtils.execute(opService, env.getDataset(), BindingFactory.empty(), env.getContext()),
                it -> ResultSetFactory.create(it, vars),
                (it, rs) -> { if (rs != null) { rs.close(); } },
                false);

        if(json == null) {
            throw new ExprTypeException("no node value obtained");
        }
        NodeValue result = JenaJsonUtils.makeNodeValue(json);
        return result;
    }

    public static <T> JsonObject benchmarkResultSet(
            Callable<T> resultSetProviderFactory, Function<? super T, ResultSet> providerToResultSet, BiConsumer<? super T, ResultSet> closeProvider, boolean includeResultSet) {
        Long resultSetSize = null;
        String errorMessage = null;
        ResultSetRewindable rsw = null;
        Stopwatch sw = Stopwatch.createStarted();

        T provider = null;
        ResultSet rs = null;
        try {
            provider = resultSetProviderFactory.call();
            rs = providerToResultSet.apply(provider);
            if(includeResultSet) {
                rsw = ResultSetFactory.copyResults(rs);
            } else {
                resultSetSize = (long)ResultSetFormatter.consume(rs);
            }
        } catch(Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failure executing benchmark request", e);
            }
            // throw new ExprTypeException("Failure executing benchmark request", e);
            errorMessage = ExceptionUtils.getStackTrace(e);
        } finally {
            if (provider != null) {
                try {
                    closeProvider.accept(provider, rs);
                } catch (Exception e2) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failure while closing resources", e2);
                    }
                }
            }
        }

        long ms = sw.stop().elapsed(TimeUnit.NANOSECONDS);
        BigDecimal s = new BigDecimal(ms).divide(new BigDecimal(1_000_000_000l));

        JsonObject json = new JsonObject();
        json.addProperty("time", s);

        if(rsw != null) {
            rsw.reset();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(baos, rsw);

            String resultSetStr = baos.toString();

            resultSetSize = (long)rsw.size();
            Gson gson = new Gson();
            JsonElement el = gson.fromJson(resultSetStr, JsonElement.class);
            json.add("result", el);
        }

        if (resultSetSize != null) {
            json.addProperty("size", resultSetSize);
        }

        if (errorMessage != null) {
            json.addProperty("error", errorMessage);
        }

        return json;
    }
}


package org.aksw.jena_sparql_api.sparql.ext.sys;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.aksw.jenax.arq.datatype.lambda.Lambda;
import org.aksw.jenax.arq.datatype.lambda.Lambdas;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase3;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;


/**
 * A function that returns an RDF term that encapsulates a lambda.
 * The last argument of fn.of is the expression, all prior arguments
 * are considered parameter variable declarations.
 *
 * <pre>
 * BIND(norse:map.computeIfAbsent('mapId', ?key, lambda) AS ?value)
 * </pre>
 *
 * @author raven
 *
 */
public class F_MapComputeIfAbsent
    extends FunctionBase3
{
    // public static final String tagLambdaOf = "lambdaOf";

    /** Symbol for a guava table in the exec cxt */
    public static Symbol symTable = SystemARQ.allocSymbol("table");

    public static Table<NodeValue, NodeValue, NodeValue> getOrCreateTable(Context cxt) {
        Table<NodeValue, NodeValue, NodeValue> result;
        if ((result = cxt.get(symTable)) == null) {
            synchronized (F_MapComputeIfAbsent.class) {
                if ((result = cxt.get(symTable)) == null) {
                    result = Tables.newCustomTable(new ConcurrentHashMap<>(), ConcurrentHashMap::new);
                    cxt.set(symTable, result);
                }
            }
        }
        return result;
    }


    @Override
    protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
        Table<NodeValue, NodeValue, NodeValue> table = getOrCreateTable(env.getContext());

        NodeValue mapId = args.get(0);
        NodeValue key = args.get(1);
        NodeValue lambdaNv = args.get(2);
        Lambda lambda = Lambdas.extract(lambdaNv);
        if (lambda.getParams().size() != 1) {
            throw new RuntimeException("Lambda must have exactly one parameter, got: " + lambda);
        }
        NodeValue result = table.row(mapId).computeIfAbsent(key, k -> Lambdas.eval(lambda, Arrays.asList(k), env));
        return result;
    }

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2, NodeValue v3) {
        throw new UnsupportedOperationException("Should never come here");
    }

    // argExprs = args.getList().subList(1,args.size()) ;

//
//    public F_MapComputeIfAbsent(ExprList args) {
//        this(tagLambdaOf, args);
//    }
//
//    protected F_MapComputeIfAbsent(String fName, ExprList args) {
//        super(fName, args) ;
//        if (args.size() == 0) {
//            identExpr = null;
//        } else {
//            identExpr = args.get(0);
//            argExprs = args.getList().subList(1,args.size()) ;
//        }
//    }
//
//    @Override
//    public NodeValue evalSpecial(Binding binding, FunctionEnv env)
//    {
//        Context cxt = env.getContext();
//        // cxt.get(funcSymbol);
//
//        //No argument returns unbound
//        if (identExpr == null) throw new ExprEvalException("CALL() has no arguments");
//
//        //One/More arguments means invoke a function dynamically
//        NodeValue func = identExpr.eval(binding, env);
//        if (func == null) throw new ExprEvalException("CALL: Function identifier unbound");
//        if (func.isIRI()) {
//            Expr e = buildFunction(func.getNode().getURI(), argExprs, env.getContext());
//            if (e == null)
//                throw new ExprEvalException("CALL: Function identifier <" + func.getNode().getURI() + "> does not identify a known function");
//            //Calling this may throw an error which we will just let bubble up
//            return e.eval(binding, env);
//        } else {
//            throw new ExprEvalException("CALL: Function identifier not an IRI");
//        }
//    }
//
//    @Override
//    public NodeValue eval(List<NodeValue> args) {
//
//
//        // eval(List, FunctionEnv) should be called.
//        throw new ARQInternalErrorException();
//    }
//
//    @Override
//    public Expr copy(ExprList newArgs) {
//        return new F_MapComputeIfAbsent(newArgs);
//    }
}

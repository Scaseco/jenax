package org.aksw.jenax.arq.util.expr;

import java.util.Arrays;
import java.util.Iterator;

import org.aksw.jenax.arq.util.exec.query.ExecutionContextUtils;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;

/** Utilities to invoke a Jena SPARQL function with a given list of Node arguments */
public class FunctionUtils {
    /** Invoke a function in the default registry by URI */
    public static Node invokeWithNodes(String iri, Node ... nodes) {
        Node result = invokeWithNodes(FunctionRegistry.get(), iri, nodes);
        return result;
    }

    /** Invoke a function in the default registry by URI */
    public static Node invokeWithNodes(FunctionRegistry registry, String iri, Node ... nodes) {
        FunctionFactory ff = registry.get(iri);
        Function fn = ff.create(iri);
        Node result = invokeWithNodes(fn, iri, nodes);
        return result;
    }
    /** Invoke a function with nodes and get the result as nodes. Passes null for the iri. */
    public static Node invokeWithNodes(Function fn, Node ... nodes) {
        return invokeWithNodes(fn, null, nodes);
    }

    public static Node invokeWithNodes(Function fn, Iterable<Node> nodes) {
        return invokeWithNodes(fn, null, nodes);
    }

    public static Node invokeWithNodes(Function fn, String iri, Node ... nodes) {
        return invokeWithNodes(fn, iri, Arrays.asList(nodes));
    }

    /** Invoke a function with nodes and get the result as nodes */
    public static Node invokeWithNodes(Function fn, String iri, Iterable<Node> nodes) {
        BindingBuilder bb = BindingFactory.builder();
        ExprList args = new ExprList();

        int i = 0;
        Iterator<Node> it = nodes.iterator();
        while (it.hasNext()) {
            Node n = it.next();
            Var v = Var.alloc("arg" + i);
            args.add(new ExprVar(v));
            bb.add(v, n);
            ++i;
        }

        Binding b = bb.build();
        FunctionEnv env = ExecutionContextUtils.createFunctionEnv();

        NodeValue nv = null;
        try {
            nv = fn.exec(b, args, iri, env);
        } catch (ExprEvalException | DatatypeFormatException e) {
            /** nothing to do */
        }
        Node result = nv == null ? null : nv.asNode();
        return result;
    }

    public static void runWithDisabledWarnOnUnknownFunction(Runnable action) {
        boolean storedValue = E_Function.WarnOnUnknownFunction;
        try {
            E_Function.WarnOnUnknownFunction = false;
            action.run();
        } finally {
            E_Function.WarnOnUnknownFunction = storedValue;
        }
    }
}

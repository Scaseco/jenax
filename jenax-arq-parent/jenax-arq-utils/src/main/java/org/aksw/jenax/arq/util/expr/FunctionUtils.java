package org.aksw.jenax.arq.util.expr;

import org.aksw.jenax.arq.util.execution.ExecutionContextUtils;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;

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
	
	/** Invoke a function with nodes and get the result as nodes */
	public static Node invokeWithNodes(Function fn, String iri, Node ... nodes) {		
		BindingBuilder bb = BindingFactory.builder();
		ExprList args = new ExprList();
		for (int i = 0; i < nodes.length; ++i) {
			Var v = Var.alloc("arg" + i);
			args.add(new ExprVar(v));
			bb.add(Var.alloc("arg" + i), nodes[i]);
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
}

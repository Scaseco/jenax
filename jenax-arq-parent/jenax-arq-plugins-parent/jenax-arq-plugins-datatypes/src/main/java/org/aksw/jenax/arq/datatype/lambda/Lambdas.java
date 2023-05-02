package org.aksw.jenax.arq.datatype.lambda;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.ExprUtils;

public class Lambdas {
    public static Lambda extract(NodeValue nv) {
        Lambda result;
        if (nv instanceof NodeValueLambda) {
            NodeValueLambda tmp = (NodeValueLambda)nv;
            result = tmp.getLambda();
        } else {
            result = extract(nv.asNode());
        }
        return result;
    }

    public static Lambda extract(Node node) {
        Lambda result;
        if (node.isLiteral()) {
            Object obj = node.getLiteralValue();
            if (obj instanceof Lambda) {
                result = (Lambda)obj;
            } else {
                throw new RuntimeException("Not a literal backed by Java type " + Lambda.class);
            }
        } else {
            throw new RuntimeException("Not a literal node");
        }
        return result;
    }

    public static Lambda create(Binding binding, ExprList args) {
        int n = args.size();
        if (n == 0) {
            throw new RuntimeException("At least 1 argument required which is an expression");
        }
        List<Expr> el = args.getList();
        List<Expr> argList = el.subList(0, n - 1);

        List<Var> argVars = argList.stream().map(e -> e.asVar()).collect(Collectors.toList());
        Set<Var> argVarsSet = new HashSet<>(argVars);
        Expr rawExpr = el.get(n - 1);

        // Remove all argVars from the binding
        Binding effBinding = BindingUtils.project(binding, binding.vars(), argVarsSet);
        Expr expr = Substitute.substitute(rawExpr, effBinding);

        Lambda result = new Lambda(argVars, expr);
        return result;
//        ExprList finalEl = new ExprList();
//        argList.forEach(finalEl::add);
//        finalEl.add(expr);

        // Node node = NodeFactory.createLiteralByValue(new E_Function(tagLambdaOf, finalEl) , RDFDatatypeExpr.get());
        // NodeValue result = NodeValue.makeNode(node);
//        return result;
    }

    public static NodeValue eval(Lambda lambda, List<NodeValue> args, FunctionEnv env) {
        List<Var> params = lambda.getParams();
        if (params.size() != args.size()) {
            throw new RuntimeException("" + args.size() + " arguments provided for lambda with " + params.size() + " parameters: Args: " + args + " lambda: " + lambda);
        }
        Iterator<Var> varIt = params.iterator();
        Iterator<NodeValue> nvIt = args.iterator();
        BindingBuilder bb = BindingFactory.builder();
        while (varIt.hasNext() && nvIt.hasNext()) {
            Var v = varIt.next();
            NodeValue nv = nvIt.next();
            bb.add(v, nv.asNode());
        }
        Binding b = bb.build();
        NodeValue result = lambda.getExpr().eval(b, env);
        return result;
    }

    public static String unparse(Lambda lambda) {
        String result =
                NodeFmtLib.strNodesNT(lambda.getParams().toArray(new Var[0]))
                + " -> "
                + ExprUtils.fmtSPARQL(lambda.getExpr());
        return result;
    }

    public static Lambda parse(String str) {
        String[] parts = str.split("->", 2);
        if (parts.length == 1) {
            throw new RuntimeException("Failed to parse SPARQL lambda; -> not found");
        }
        List<Node> nodes = NodeUtils.parseNodes(parts[0], new ArrayList<>());
        List<Var> vars = nodes.stream().map(node -> (Var)node).collect(Collectors.toList());

        Expr expr = ExprUtils.parse(parts[1]);
        Lambda result = new Lambda(vars, expr);
        return result;
    }
}

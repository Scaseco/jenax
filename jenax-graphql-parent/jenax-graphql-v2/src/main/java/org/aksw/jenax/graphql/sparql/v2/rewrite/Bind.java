package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;

public class Bind {

    interface BindingMapper<T>
        extends BiFunction<Binding, FunctionEnv, T> {
    }

    public static class BindingMapperVar
        implements BindingMapper<Node>
    {
        protected Var var;

        public BindingMapperVar(Var var) {
            super();
            this.var = var;
        }

        @Override
        public Node apply(Binding t, FunctionEnv u) {
            return t.get(var);
        }

        @Override
        public String toString() {
            return "Binder(" + var + ")";
        }
    }

    public static class BindingMapperExpr
        implements BindingMapper<Node>
    {
        protected Expr expr;

        public BindingMapperExpr(Expr expr) {
            super();
            this.expr = expr;
        }

        @Override
        public Node apply(Binding t, FunctionEnv u) {
            NodeValue nv = expr.eval(t, u);
            Node result = nv == null ? null : nv.asNode();
            return result;
        }

        @Override
        public String toString() {
            return "Binder(" + expr + ")";
        }
    }


    public static BindingMapperVar var(String varName) {
        return var(Var.alloc(varName));
    }

    public static BindingMapperVar var(Var var) {
        return new BindingMapperVar(var);
    }


    public static class BindingMapperVars
        implements BindingMapper<Node>
    {
        protected List<Var> vars;

        public BindingMapperVars(List<Var> vars) {
            super();
            this.vars = vars;
        }

        @Override
        public Node apply(Binding t, FunctionEnv u) {
            List<Node> nodes = vars.stream().map(t::get).toList();
            // NodeList nodeList = new NodeListImpl(nodes);
            // HACK We don't have the list datatype here
            String str = nodes.stream().map(n -> n == null ? "UNDEF" : fmtNode(n)).collect(Collectors.joining(" "));
            return NodeFactory.createLiteralString(str);
        }

        @Override
        public String toString() {
            return "Binder(" + vars + ")";
        }

        public static String fmtNode(Node node) {
            // Format numbers using turtle - otherwise use n-triples
            return node.isLiteral() && node.getLiteralValue() instanceof Number
                    ? NodeFmtLib.strTTL(node)
                    : NodeFmtLib.strNT(node);
        }
    }


    public static BindingMapperVars vars(String ... varNames) {
        List<Var> list = Var.varList(Arrays.asList(varNames));
        return vars(list);
    }

    public static BindingMapperVars vars(List<Var> vars) {
        return new BindingMapperVars(vars);
    }

    public static BindingMapperExpr expr(Expr expr) {
        return new BindingMapperExpr(expr);
    }

    public static BindingMapperExpr TRUE() {
        return new BindingMapperExpr(NodeValue.TRUE);
    }

    public static BindingMapperExpr FALSE() {
        return new BindingMapperExpr(NodeValue.FALSE);
    }
}

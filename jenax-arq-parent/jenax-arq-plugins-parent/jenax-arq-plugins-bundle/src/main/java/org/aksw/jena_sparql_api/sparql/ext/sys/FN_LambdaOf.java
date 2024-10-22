package org.aksw.jena_sparql_api.sparql.ext.sys;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.datatype.lambda.Lambda;
import org.aksw.jenax.arq.datatype.lambda.NodeValueLambda;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Rename;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.service.enhancer.impl.util.VarScopeUtils;

/**
 * A function that returns an RDF term that encapsulates a lambda.
 * The last argument of fn.of is the expression, all prior arguments
 * are considered parameter variable declarations.
 *
 * <pre>
 * BIND(norse:fn.of(?x, ?x + 1) AS ?lambda)
 * BIND(norse:fn.call(?lambda, 1) AS ?y) # expected: ?y == 2
 * </pre>
 *
 * @author raven
 *
 */
public class FN_LambdaOf
    extends FunctionBase
{
    public static void main(String[] args) {


        String str = String.join("\n",
                "PREFIX eg: <http://www.example.org/>",
                "PREFIX norse: <https://w3id.org/aksw/norse#>",
                "PREFIX sys: <http://jsa.aksw.org/fn/sys/>",
                "SELECT ?fn {",
                "  BIND(norse:fn.of(?x, IRI(CONCAT(STR(eg:), STR(sys:nextLong())))) AS ?fn)",
                "  LATERAL {",
                "      { BIND(norse:map.computeIfAbsent('myMap', 'key1', ?fn) AS ?v) }",
                "    UNION",
                "      { BIND(norse:map.computeIfAbsent('myMap', 'key1', ?fn) AS ?v) }",
                "    UNION",
                "      { BIND(norse:map.computeIfAbsent('myMap', 'key2', ?fn) AS ?v) }",
                "  }",
                "}"
                );

        str = String.join("\n",
                "PREFIX norse: <https://w3id.org/aksw/norse#>",
                "SELECT ?helloFn ?msg {",
                "  BIND('Hi' AS ?salutation)",
                "  BIND(norse:fn.of(?x, CONCAT(?salutation, ' ', ?x)) AS ?helloFn)",
                "  BIND(norse:fn.call(?helloFn, 'Lorenz') AS ?msg)",
                "}"
                );

        System.out.println(str);
        Query query = QueryFactory.create(str);
        try (QueryExec qe = QueryExec.newBuilder()
            .dataset(DatasetGraphFactory.create())
            .query(query).build()) {
            System.out.println(ResultSetFormatter.asText(ResultSet.adapt(qe.select())));
        }
    }

    public FN_LambdaOf() {
    }

    @Override
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
        int n = args.size();
        if (n == 0) {
            throw new RuntimeException("At least 1 argument required which is an expression");
        }
        List<Expr> scopedExprs = args.getList();
        List<Expr> scopedArgList = scopedExprs.subList(0, n - 1);

        List<Var> scopedArgVars = scopedArgList.stream().map(e -> e.asVar()).collect(Collectors.toList());
        Set<Var> scopedArgVarsSet = new HashSet<>(scopedArgVars);
        Expr scopedRawExpr = scopedExprs.get(n - 1);

        // Remove all argVars from the binding
        Binding effBinding = BindingUtils.project(binding, binding.vars(), scopedArgVarsSet);
        Expr scopedExpr = Substitute.substitute(scopedRawExpr, effBinding);

        List<Var> unscopedArgVars = scopedArgVars.stream().map(v -> (Var)Rename.reverseVarRename(v)).toList();
        Expr unscopedExpr = ExprUtils.reverseVarRename(scopedExpr);

        Lambda lambda = new Lambda(unscopedArgVars, unscopedExpr);
        NodeValue result = new NodeValueLambda(lambda);
        return result;
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        throw new RuntimeException("Should not be called");
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        // TODO Validate
    }
}

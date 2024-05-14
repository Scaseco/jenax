package org.aksw.jena_sparql_api.sparql.ext.sys;

import java.util.List;

import org.aksw.jenax.arq.datatype.lambda.Lambda;
import org.aksw.jenax.arq.datatype.lambda.Lambdas;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionEnv;

public class FN_LambdaCall
    extends FunctionBase
{
    @Override
    protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
        NodeValue lambdaNv = args.get(0);
        List<NodeValue> realArgs = args.subList(1, args.size());
        Lambda lambda = Lambdas.extract(lambdaNv);
        NodeValue result = Lambdas.eval(lambda, realArgs, env);
        return result;
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        throw new UnsupportedOperationException("Should never be called");
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
    }
}

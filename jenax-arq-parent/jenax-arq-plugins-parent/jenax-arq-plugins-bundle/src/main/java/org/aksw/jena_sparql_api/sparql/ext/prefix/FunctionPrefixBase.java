package org.aksw.jena_sparql_api.sparql.ext.prefix;

import java.util.List;

import org.aksw.jenax.arq.util.exec.query.FunctionEnvUtils;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.sparql.function.FunctionEnv;

public abstract class FunctionPrefixBase
    extends FunctionBase1
{
    @Override
    protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
        NodeValue arg = args.get(0);

        NodeValue result = null;
        if (isValidArg(arg)) {
            String str = arg.asUnquotedString();

            PrefixMap pm = FunctionEnvUtils.getActivePrefixes(env);
            result = process(pm, str);

        } else {
            NodeValue.raise(new ExprTypeException("datatype: Neither IRI nor string: " + arg));
        }

        if (result == null) {
            NodeValue.raise(new ExprTypeException("prefix function evaluated to null: " + this.getClass().getName() + " with argument " + arg));
        }

        return result;
    }

    public abstract boolean isValidArg(NodeValue arg);

    public abstract NodeValue process(PrefixMap prefixMap, String str);

    @Override
    public NodeValue exec(NodeValue v) {
        throw new UnsupportedOperationException("Should not be called");
    }
}

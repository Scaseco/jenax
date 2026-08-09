package org.aksw.jena_sparql_api.sparql.ext.sys;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * Relation to other system functions:
 *
 * norse:sys:map.get: (what this class is about): Get a node from the execution context.
 * norse.sys:getenv: get an environment variable such as "HOME" (corresponds to bash's $HOME).
 * norse.sys:getProperty: get a Java property such as "user.home".
 */
public class SysMapUtils {
    public static Symbol symMap = SystemARQ.allocSymbol("sysMap");

    public static Map<NodeValue, NodeValue> getMap(Context cxt) {
        Map<NodeValue, NodeValue> result = (cxt != null)
            ? cxt.get(symMap)
            : null;
        return result;
    }

    public static Map<NodeValue, NodeValue> getOrCreateMap(Context cxt) {
        Map<NodeValue, NodeValue> result = (cxt != null)
            ? cxt.computeIfAbsent(symMap, sym -> new ConcurrentHashMap<>())
            : null;
        return result;
    }

    private static NodeValue get(List<NodeValue> args, FunctionEnv env, boolean strict) {
        Map<NodeValue, NodeValue> map = getMap(env.getContext());
        NodeValue value = null;
        if (map != null) {
            NodeValue key = args.get(1);
            value = map.get(key);
        } else {
            if (strict) {
                throw new RuntimeException("SysMap: no map");
            } else {
                NodeValue.raise(new ExprEvalException("SysMap: no map"));
            }
        }
        if (value == null) {
            if (strict) {
                throw new RuntimeException("SysMap: no value");
            } else {
                NodeValue.raise(new ExprEvalException("SysMap: no value"));
            }
        }
        return value;
    }

    public class FN_SysMapGet
        extends FunctionBase2
    {
        @Override
        protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
            return SysMapUtils.get(args, env, false);
        }

        @Override
        public NodeValue exec(NodeValue v1, NodeValue v2) {
            throw new UnsupportedOperationException("Should never come here");
        }
    }

    public class FN_SysMapGetStrict
        extends FunctionBase2
    {
        @Override
        protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
            return SysMapUtils.get(args, env, true);
        }

        @Override
        public NodeValue exec(NodeValue v1, NodeValue v2) {
            throw new UnsupportedOperationException("Should never come here");
        }
    }
}

package org.aksw.jena_sparql_api.sparql.ext.benchmark;

import java.util.List;

import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourceWithLocalCache;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FN_SparqlQueryRewrite_CacheGroupBy
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
        if (args.size() != 1) {
            throw new QueryBuildException("Function '"+Lib.className(this)+"' takes one argument") ;
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
        NodeValue queryNv = args.get(0);

        if (queryNv == null) {
            throw new VariableNotBoundException("The query for benchmark was not bound");
        }

        Node queryNode = queryNv.getNode();
        String queryStr = queryNode.getLiteralLexicalForm();
        Query query = QueryFactory.create(queryStr);

        Query rewrittenQuery = RdfDataSourceWithLocalCache.TransformInjectCacheSyntax.rewriteQuery(query);
        String str = rewrittenQuery.toString();
        NodeValue result = NodeValue.makeString(str);
        return result;
    }
}


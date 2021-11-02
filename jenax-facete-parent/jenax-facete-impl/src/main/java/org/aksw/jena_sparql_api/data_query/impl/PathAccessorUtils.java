package org.aksw.jena_sparql_api.data_query.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;

public class PathAccessorUtils {
    public static <P> Map<Node, P> getPathsMentioned(Expr expr, Function<? super Node, ? extends P> tryMapPath) {
        Map<Node, P> result = Streams.stream(Traverser.forTree(ExprUtils::getSubExprs).depthFirstPreOrder(expr).iterator())
            .filter(Expr::isConstant)
            .map(org.apache.jena.sparql.util.ExprUtils::eval)
            .map(NodeValue::asNode)
            .map(node -> Maps.immutableEntry(node, tryMapPath.apply(node)))
            .filter(e -> e.getValue() != null)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> u));

        return result;
    }
}

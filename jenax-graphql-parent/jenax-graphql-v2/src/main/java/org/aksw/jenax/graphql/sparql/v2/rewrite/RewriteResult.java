package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.Map;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilder;
import org.aksw.jenax.graphql.sparql.v2.model.ElementNode;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;

public class RewriteResult<K> {
    public static record SingleResult<K>(ElementNode rootElementNode, AggStateBuilder<Binding, FunctionEnv, K, Node> rootAggBuilder, boolean isSingle) {}

    SingleResult<K> root;
    Map<String, SingleResult<K>> map;

    public SingleResult<K> root() {
        return root;
    }

    public Map<String, SingleResult<K>> map() {
        return map;
    }
}

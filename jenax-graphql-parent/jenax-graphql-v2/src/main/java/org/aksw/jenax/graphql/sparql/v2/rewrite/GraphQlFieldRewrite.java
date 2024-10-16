package org.aksw.jenax.graphql.sparql.v2.rewrite;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilder;
import org.aksw.jenax.graphql.sparql.v2.model.ElementNode;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;

/**
 * Intermediate state for generating the final SPARQL query.
 *
 * @param <K>
 */
public record GraphQlFieldRewrite<K>(
    ElementNode rootElementNode,
    AggStateBuilder<Binding, FunctionEnv, K, Node> rootAggBuilder,
    boolean isSingle,
    graphql.language.Node<?> graphQlNode) {
}

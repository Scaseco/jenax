package org.aksw.jenax.graphql.sparql.v2.api.low;

import java.util.Map;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.model.ElementNode;
import org.aksw.jenax.graphql.sparql.v2.rewrite.GraphQlFieldRewrite;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;

/**
 * The query mapping contains the final sparql query, the corresponding accumulator, and
 * any additional information needed to produce the overall object notation result.
 *
 * The main difference to {@link GraphQlFieldRewrite} is that the {@link ElementNode} has been converted to a query.
 */
public record QueryMapping<K>(
    String name, Var stateVar, Node rootStateId, Query query,
    Map<?, Map<Var, Var>> stateVarMap,
    AggStateGon<Binding, FunctionEnv, K, Node> agg, boolean isSingle,
    GraphQlFieldRewrite<K> fieldRewrite // Needed for annotations such as @debug
    ) {
}

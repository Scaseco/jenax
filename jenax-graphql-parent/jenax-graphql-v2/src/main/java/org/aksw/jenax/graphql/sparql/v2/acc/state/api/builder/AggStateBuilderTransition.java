package org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder;

public interface AggStateBuilderTransition<I, E, K, V>
    extends AggStateBuilder<I, E, K, V>
{
    Object getMatchStateId();
}
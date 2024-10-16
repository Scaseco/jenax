package org.aksw.jenax.graphql.sparql.v2.acc.state.api;

public interface AccStateTypeTransition<I, E, K, V>
    extends AccStateGon<I, E, K, V>
{
    Object getMatchStateId();
}

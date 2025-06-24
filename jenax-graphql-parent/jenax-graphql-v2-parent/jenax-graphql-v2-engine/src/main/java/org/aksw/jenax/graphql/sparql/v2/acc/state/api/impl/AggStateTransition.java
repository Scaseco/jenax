package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeTransition;

// Base class for agg states that transitions on a state id
// edge and fragment (condition)
public interface AggStateTransition<I, E, K, V>
    extends AggStateGon<I, E, K, V>
{
    Object getMatchStateId();

    // AggStateTypeProduceEntry<I, E, K, V> setTargetAgg(AggStateTypeProduceNode<I, E, K, V> targetAgg);
    // AggJsonEdge setSingle(boolean value);
    @Override
    AccStateTypeTransition<I, E, K, V> newAccumulator();
}

package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

public interface AggStateGon<I, E, K, V>
    extends AggState<I, E>
{
    GonType getGonType();

    @Override
    AccStateGon<I, E, K, V> newAccumulator();
}

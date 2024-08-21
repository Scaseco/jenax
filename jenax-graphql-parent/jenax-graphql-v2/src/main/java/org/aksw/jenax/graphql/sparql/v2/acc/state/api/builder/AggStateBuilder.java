package org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

public interface AggStateBuilder<I, E, K, V> {
    GonType getGonType();
    AggStateGon<I, E, K, V> newAggregator();
}

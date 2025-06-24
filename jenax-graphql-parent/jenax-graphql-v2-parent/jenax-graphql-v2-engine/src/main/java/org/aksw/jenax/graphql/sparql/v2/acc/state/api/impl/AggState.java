package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccState;

public interface AggState<I, E> {
    AccState<I, E> newAccumulator();
}

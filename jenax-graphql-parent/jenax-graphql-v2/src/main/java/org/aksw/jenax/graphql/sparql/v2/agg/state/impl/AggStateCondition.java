package org.aksw.jenax.graphql.sparql.v2.agg.state.impl;

import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeTransition;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateCondition;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTransition;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

public class AggStateCondition<I, E, K, V>
    implements AggStateTransition<I, E, K, V>
{
    protected Object matchStateId;
    protected AggStateGon<I, E, K, V> subAgg;

    protected AggStateCondition(Object matchStateId, AggStateGon<I, E, K, V> subAgg) {
        super();
        this.matchStateId = Objects.requireNonNull(matchStateId);
        this.subAgg = Objects.requireNonNull(subAgg);
    }

    public static <I, E, K, V> AggStateCondition<I, E, K, V> of(Object matchStateId, AggStateGon<I, E, K, V> subAgg) {
        return new AggStateCondition<>(matchStateId, subAgg);
    }

    @Override
    public Object getMatchStateId() {
        return matchStateId;
    }

    @Override
    public GonType getGonType() {
        return subAgg.getGonType();
    }

    @Override
    public AccStateTypeTransition<I, E, K, V> newAccumulator() {
        AccStateGon<I, E, K, V> subAcc = subAgg.newAccumulator();
        return new AccStateCondition<>(matchStateId, subAcc);
    }
}

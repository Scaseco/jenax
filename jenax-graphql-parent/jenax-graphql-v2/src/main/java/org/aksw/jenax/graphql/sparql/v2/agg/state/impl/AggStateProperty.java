package org.aksw.jenax.graphql.sparql.v2.agg.state.impl;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeProduceEntry;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateProperty;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStatePropertyBase;

/** AggState for a preset member key. */
public class AggStateProperty<I, E, K, V>
    extends AggStatePropertyBase<I, E, K, V>
{
    /** The member key being aggregated */
    /** The aggregator for the value */
    protected AggStateGon<I, E, K, V> subAgg;
    // protected AggStateTypeProduceNode<I, E, K, V> subAgg;

    protected AggStateProperty(Object matchStateId, K memberKey, boolean isSingle, AggStateGon<I, E, K, V> subAgg) {
        super(matchStateId, memberKey, isSingle);
        this.subAgg = subAgg;
    }

    public static <I, E, K, V> AggStateProperty<I, E, K, V> many(Object matchFieldId, K memberKey, AggStateGon<I, E, K, V> subAgg) {
        return of(matchFieldId, memberKey, false, subAgg);
    }

    public static <I, E, K, V> AggStateProperty<I, E, K, V> one(Object matchFieldId, K memberKey, AggStateGon<I, E, K, V> subAgg) {
        return of(matchFieldId, memberKey, true, subAgg);
    }

    // AggStateTypeProduceNode
    public static <I, E, K, V> AggStateProperty<I, E, K, V> of(Object matchFieldId, K memberKey, boolean isSingle, AggStateGon<I, E, K, V> subAgg) {
        return new AggStateProperty<>(matchFieldId, memberKey, isSingle, subAgg);
    }

    @Override
    public AccStateTypeProduceEntry<I, E, K, V> newAccumulator() {
        AccStateGon<I, E, K, V> valueAcc = subAgg.newAccumulator();
        AccStateProperty<I, E, K, V> result = new AccStateProperty<>(matchStateId, memberKey, valueAcc, isSingle);
        valueAcc.setParent(result);
        return result;
    }
}
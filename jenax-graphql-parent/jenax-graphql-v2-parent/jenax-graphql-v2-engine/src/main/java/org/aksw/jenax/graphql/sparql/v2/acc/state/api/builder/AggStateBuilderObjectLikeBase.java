package org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTransition;

public abstract class AggStateBuilderObjectLikeBase<I, E, K, V>
    implements AggStateBuilder<I, E, K, V>
{
    // protected Map<K, AggStateBuilderEdge<I, E, K, V>> propertyMappers = new LinkedHashMap<>();
    protected List<AggStateBuilderTransitionMatch<I, E, K, V>> edgeMappers = new ArrayList<>();
    // protected List<AggStateBuilder<I, E, K, V>> edgeMappers = new ArrayList<>();


    public List<AggStateBuilderTransitionMatch<I, E, K, V>> getPropertyMappers() {
        return edgeMappers;
    }

    public void addMember(AggStateBuilderTransitionMatch<I, E, K, V> member) {
        Objects.requireNonNull(member);
        edgeMappers.add(member);
    }

    public AggStateTransition<I, E, K, V>[] buildSubAggs() {
        int n = edgeMappers.size();
        @SuppressWarnings("unchecked")
        // AggStateTypeProduceEntry<I, E, K, V>[] subAggs = new AggStateTypeProduceEntry[n];
        AggStateTransition<I, E, K, V>[] subAggs = new AggStateTransition[n];
        int i = 0;

        for (AggStateBuilderTransitionMatch<I, E, K, V> entry : edgeMappers) {
        // for (AggStateBuilder<I, E, K, V> entry : edgeMappers) {
            subAggs[i] = entry.newAggregator();
            ++i;
        }
        //AggStateFragment<I, E, K, V> result = AggStateFragment.of(subAggs);
        return subAggs;
    }
}

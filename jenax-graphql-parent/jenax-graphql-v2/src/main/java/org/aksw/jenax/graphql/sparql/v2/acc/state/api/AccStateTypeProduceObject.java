package org.aksw.jenax.graphql.sparql.v2.acc.state.api;

import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

/** Accumulator that produces objects. */
public interface AccStateTypeProduceObject<I, E, K, V>
    extends AccStateTypeProduceNode<I, E, K, V>
{
    @Override
    default GonType getGonType() {
        return GonType.OBJECT;
    }
    // Object getValue();
}

package org.aksw.jenax.graphql.sparql.v2.acc.state.api;

import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

/**
 * Accumulator that produces entries, i.e. (key, value) pairs.
 * The keys emitted by an AccStateTypeEntry can be dynamic - both in value and amount.
 * From the perspective of an ObjectNotation writer, this method invokes the
 * ".name(key)" method.
 */
public interface AccStateTypeProduceEntry<I, E, K, V>
    extends AccStateTypeTransition<I, E, K, V>
    // extends AccStateGon<I, E, K, V>
//    extends AccStateTypeNonObject<I, E, K, V>
{
    @Override
    default GonType getGonType() {
        return GonType.ENTRY;
    }

    Object getMatchStateId();
}

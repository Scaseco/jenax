package org.aksw.jenax.graphql.sparql.v2.acc.state.api;

/**
 * AccState that PRODUCES literals, objects or arrays - but not entries.
 * The parent must produce ENTRY or ARRAY (but not object).
 */
public interface AccStateTypeProduceNode<I, E, K, V>
    extends AccStateGon<I, E, K, V>
{
//    @Override
//    AccStateTypeNonObject<I, E, K, V> getParent();
}

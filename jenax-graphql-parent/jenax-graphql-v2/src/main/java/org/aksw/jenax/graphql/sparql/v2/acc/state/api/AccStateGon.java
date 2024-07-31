package org.aksw.jenax.graphql.sparql.v2.acc.state.api;

import java.io.IOException;
import java.util.Iterator;

import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

/**
 * AccState for producing generalized object notation (gon) structures.
 *
 * @param <I> Input type (e.g. Binding)
 * @param <E> Environment type (such as FunctionEnv, ExecutionContext)
 * @param <K> Type of fields in object output
 * @param <V> Type of primitives in object output
 */
public interface AccStateGon<I, E, K, V>
    extends AccState<I, E>
{
    /** The immediate parent of this AccState. */
    @Override
    AccStateGon<I, E, K, V> getParent();

    /** The GON type which this accumulator produces. */
    GonType getGonType();

    /** The ancestor to which to attach produced values. */
//    @Override
//    AccStateGon<I, E, K, V> getValueParent();

    @Override
    default AccStateGon<I, E, K, V> getRoot() {
        AccStateGon<I, E, K, V> parent = getParent();
        return parent == null ? this : parent.getRoot();
    }

    void setParent(AccStateGon<I, E, K, V> parent);

    /** The state to which to backtrack if an input cannot be processed by the current state */
    // void setAncestorState(AccLateralStructure<I, E, K, V> ancestorState);

    void setContext(AccContext<K, V> context);

    /**
     * FIXME What exactly is id? Is it the value by which this acc connects to the parent?
     * The context and skipOutput flag are stored on the accumulator until end is called.
     */

    @Override
    AccStateGon<I, E, K, V> transition(Object inputStateId, I input, E env) throws IOException;

    @Override
    Iterator<? extends AccStateGon<I, E, K, V>> children();
}

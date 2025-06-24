package org.aksw.jenax.graphql.sparql.v2.acc.state.api;

import java.io.IOException;
import java.util.Iterator;

public interface AccState<I, E> {
    AccState<I, E> getParent();

    default AccState<I, E> getRoot() {
        AccState<I, E> parent = getParent();
        return parent == null ? this : parent.getRoot();
    }

    // AccState<I, E> getValueParent();

    /** Get the id of the state for which this accumulator is notified. */
    Object getStateId();

    /**
     * FIXME What exactly is id? Is it the value by which this acc connects to the parent?
     * The context and skipOutput flag are stored on the accumulator until end is called.
     */
    void begin(Object stateId, I parentInput, E env, boolean skipOutput) throws IOException;

    boolean hasBegun();

    /**
     *
     * @param inputStateId The stateId, typically extracted from the input by the driver. (Rationale: Only the driver needs to know how to extract the state from an input).
     * @param binding
     * @param env
     * @return
     * @throws IOException
     */
    AccState<I, E> transition(Object inputStateId, I binding, E env) throws IOException;

    void end() throws IOException;

    /** Sub-accumulators must be enumerated in the correct order. */
    Iterator<? extends AccState<I, E>> children();
}

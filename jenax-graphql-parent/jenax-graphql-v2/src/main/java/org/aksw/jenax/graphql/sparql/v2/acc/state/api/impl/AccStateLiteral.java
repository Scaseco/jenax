package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateBase;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeProduceNode;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;

/**
 * AccStateLiteral:
 * Extracts a value from the input and forwards it to the writer.
 * ValueParent must be 'entry' or 'array.
 *
 * Always accepts the current state. (Matches the same stateId as the parent)
 * Never transitions into a new state.
 */
public class AccStateLiteral<I, E, K, V>
    extends AccStateBase<I, E, K, V>
    implements AccStateTypeProduceNode<I, E, K, V>
    // implements AccLateralNode<D, C, O>
{
    protected BiFunction<I, E, ? extends V> inputToValue;
    protected V currentValue;

    protected AccStateLiteral(BiFunction<I, E, ? extends V> inputToValue) {
        this.inputToValue = Objects.requireNonNull(inputToValue);
    }

    @Override
    public GonType getGonType() {
        return GonType.LITERAL;
    }

//    @Override
//    public AccStateGon<I, E, K, V> getParent() {
//        return (AccStateTypeNonObject<I, E, K, V>)super.getParent();
//    }

    public static <I, E, K, V> AccStateLiteral<I, E, K, V> of(BiFunction<I, E, ? extends V> inputToValue) {
        return new AccStateLiteral<>(inputToValue);
    }

    @Override
    protected void beginActual() throws IOException {
        currentValue = inputToValue.apply(parentInput, null);
    }

    @Override
    public AccStateGon<I, E, K, V> transitionActual(Object inputStateId, I input, E env) {
        // Literals reject all edges (indicated by null)
        return null;
    }

    @Override
    public void endActual() throws IOException {
        if (!skipOutput) {
            // O effectiveValue = value == null ? new RdfNull() : value;
            //O effectiveValue = value;

            if (context.isSerialize()) {
                ObjectNotationWriter<K, V> writer = context.getJsonWriter();
                writer.value(currentValue);
            }

            if (context.isMaterialize()) {
            }
//            if (context.isMaterialize() && parent != null) {
//                //parent.acceptContribution(effectiveValue, context);
//            }
        }
        // super.end(context);
    }

    @Override
    public String toString() {
        return "AccStateLiteral(source: " + currentSourceNode + ")";
    }

    @Override
    public Iterator<? extends AccStateGon<I, E, K, V>> children() {
        return Collections.emptyIterator();
    }
}

package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateBase;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeProduceEntry;

/**
 * This mapper only generates the keys - it does not generate the enclosing start/end object tags.
 *
 * {
 *   k1: [k1v1, k1v2]
 *   k2: k2v
 * }
 */
public class AccStateMap<I, E, K, V>
    extends AccStateBase<I, E, K, V>
    implements AccStateTypeProduceEntry<I, E, K, V>
{
    // AccStateTypeProduceObject
    // protected AccStateGon<I, E, K, V> valueParent;

    protected BiFunction<I, E, ? extends K> inputToKeyMapper;
    protected BiPredicate<I, E> testIfSingle;
    protected AccStateGon<I, E, K, V> subAcc;

    protected Object currentKey;
    protected boolean isCurrentKeySingle;

    protected int seenTargetCount = 0;
    protected boolean skipOutputStartedHere = false;

    protected Object matchStateId;

    // public AccStateMap(Object matchStateId, BiFunction<I, E, ? extends K> inputToKeyMapper, BiPredicate<I, E> testIfSingle, AccStateTypeProduceNode<I, E, K, V> subAcc) {
    public AccStateMap(Object matchStateId, BiFunction<I, E, ? extends K> inputToKeyMapper, BiPredicate<I, E> testIfSingle, AccStateGon<I, E, K, V> subAcc) {
        super();
        this.matchStateId = matchStateId;
        this.inputToKeyMapper = Objects.requireNonNull(inputToKeyMapper);
        this.testIfSingle = testIfSingle;
        this.subAcc = subAcc; // TODO Validate gon type = produce node
    }

//    @Override
//    public AccStateTypeProduceObject<I, E, K, V> getParent() {
//        return (AccStateTypeProduceObject<I, E, K, V>)super.getParent();
//    }
//    @Override
//    public AccStateTypeProduceObject<I, E, K, V> getValueParent() {
//        return valueParent;
//    }

    @Override
    public AccStateGon<I, E, K, V> transitionActual(Object inputStateId, I input, E env) throws IOException {
        AccStateGon<I, E, K, V> result;

        if (!Objects.equals(inputStateId, matchStateId)) {
            result = null;
        } else {
            K key = inputToKeyMapper.apply(input, env);

            if (!Objects.equals(key, currentKey)) {

                // End a previously started array
                endOpenArray();

                // Reset counter
                seenTargetCount = 0;
                skipOutputStartedHere = false;

                currentKey = key;

//                if (subAcc.hasBegun()) {
//                    subAcc.end();
//                }

                if (!skipOutput) {
                    if (context.isSerialize()) {
                        context.getJsonWriter().name(key);
                    }
                }

                isCurrentKeySingle = testIfSingle.test(input, env);

                if (!isCurrentKeySingle) {
                    if (!skipOutput) {
                        if (context.isSerialize()) {
                            context.getJsonWriter().beginArray();
                        }
                    }
                }

            }


            skipOutputStartedHere = isCurrentKeySingle && seenTargetCount >= 1;
            subAcc.begin(key, input, env, skipOutput || skipOutputStartedHere);
            ++seenTargetCount;

            result = subAcc;
        }
        return result;
    }

    protected void endOpenArray() throws IOException {
        if (currentKey != null) {
            if (!isCurrentKeySingle) {
                if (!skipOutput) {
                    if (context.isSerialize()) {
                        context.getJsonWriter().endArray();
                    }
                }
            }
            currentKey = null;
        }
    }

    @Override
    public void endActual() throws IOException {
//        if (subAcc.hasBegun()) {
//            subAcc.end();
//        }
        endOpenArray();
//        if (!skipOutput) {
//            // O effectiveValue = value == null ? new RdfNull() : value;
//            O effectiveValue = value;
//
//            if (context.isSerialize()) {
//                RdfObjectNotationWriter writer = context.getJsonWriter();
//                if (value == null) {
//                    writer.nullValue();
//                } else {
//                    // writer.value(value.getAsLiteral().getInternalId());
//                    throw new RuntimeException("fix implementation");
//                }
//            }
//
//            if (context.isMaterialize() && parent != null) {
//                parent.acceptContribution(effectiveValue, context);
//            }
//        }
    }

    @Override
    public String toString() {
        return "AccStateMap(matches: " + matchStateId + ", source: " + currentSourceNode + ", target: " + subAcc + ")";
    }

    @Override
    public Iterator<? extends AccStateGon<I, E, K, V>> children() {
        return List.of(subAcc).iterator();
    }

    @Override
    public Object getMatchStateId() {
        return matchStateId;
    }
}

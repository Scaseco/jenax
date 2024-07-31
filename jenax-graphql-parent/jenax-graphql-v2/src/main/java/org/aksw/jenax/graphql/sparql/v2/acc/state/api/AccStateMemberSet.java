package org.aksw.jenax.graphql.sparql.v2.acc.state.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/** Common base class for accumulators of Objects and Fragments. */
public abstract class AccStateMemberSet<I, E, K, V>
    extends AccStateBase<I, E, K, V>
{
    protected Map<Object, Integer> stateIdToIndex = new HashMap<>();
    protected AccStateGon<I, E, K, V>[] edgeAccs;

    protected int currentFieldIndex = -1;
    protected AccStateGon<I, E, K, V> currentFieldAcc = null;

    protected AccStateMemberSet(Map<Object, Integer> stateIdToIndex, AccStateGon<I, E, K, V>[] edgeAccs) {
        super();
        this.stateIdToIndex = stateIdToIndex;
        this.edgeAccs = edgeAccs;
    }

    @Override
    public Iterator<? extends AccStateGon<I, E, K, V>> children() {
        return Arrays.asList(edgeAccs).iterator();
    }

    @Override
    public AccStateGon<I, E, K, V> transitionActual(Object inputStateId, I input, E env) throws IOException {
        // Object inputFieldId = getInputStateId(input, env);

        AccStateGon<I, E, K, V> result = null;
        Integer inputFieldIndex = stateIdToIndex.get(inputStateId);
        if (inputFieldIndex != null) {
            int requestedFieldIndex = inputFieldIndex.intValue();

            // Detect if the requested field comes before the current field
            // This should only happen if there is a new source
            // Sanity check: Check that the source of this field is different from the current sourceNode
            if (requestedFieldIndex < currentFieldIndex) {
                AccStateGon<I, E, K, V> edgeAcc = edgeAccs[requestedFieldIndex];
                // Node inputSource = TripleUtils.getSource(input, edgeAcc.isForward());

//                if (Objects.equals(inputSource, currentSourceNode)) {
//                    throw new RuntimeException("fields appear to have arrived out of order - should not happen");
//                    // TODO Improve error message: on sourceNode data for field [] was expected to arrive after field []
//                }
            }

            // Skip over the remaining fields - allow them to produce
            // values such as null or empty arrays
            for (int i = currentFieldIndex + 1; i < requestedFieldIndex; ++i) {
                AccStateGon<I, E, K, V> edgeAcc = edgeAccs[i];
                edgeAcc.begin(null, input, env, skipOutput);
                edgeAcc.end();
            }

            currentFieldIndex = requestedFieldIndex;
            currentFieldAcc = edgeAccs[requestedFieldIndex];

            // boolean isForward = currentFieldAcc.isForward();
            // Node edgeInputSource = TripleUtils.getSource(input, isForward);
            Object edgeInputSource = null;

            if (!Objects.equals(edgeInputSource, currentSourceNode)) {
                throw new RuntimeException("should not happen - node accumulator at source [" + currentSourceNode + "] but edge claims source at [" + edgeInputSource + "]");
            }

            currentFieldAcc.begin(edgeInputSource, input, env, skipOutput);
            result = currentFieldAcc.transition(inputStateId, input, env);
            // result = currentFieldAcc;
        }
        return result;
    }

    /** Internal method, use only for debugging/testing */
//    public void addEdge(AccLateralStructMember<I, E, K, V> subAcc) {
//        // TODO Lots of array copying!
//        // We should add a builder for efficient additions and derive the more efficient array version from it.
//        Object fieldId = subAcc.getMatchFieldId();
//        int fieldIndex = edgeAccs.length;
//        stateIdToIndex.put(fieldId, fieldIndex);
//        edgeAccs = Arrays.copyOf(edgeAccs, fieldIndex + 1);
//        edgeAccs[fieldIndex] = subAcc;
//        subAcc.setParent(this);
//    }
}

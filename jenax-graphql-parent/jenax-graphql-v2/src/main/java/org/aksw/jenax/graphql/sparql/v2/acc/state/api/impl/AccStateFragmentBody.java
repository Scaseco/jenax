package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateMemberSet;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeTransition;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

/**
 * AccState that creates a new object whether a new objectId is encountered.
 *
 * [stateId, v0, ..., vN]
 * [      1, objectId1  ]
 * [      2, member1    ]
 * [      3, member2    ]
 * [      1, objectId2  ]
 *
 * @param <I>
 * @param <E>
 * @param <K>
 * @param <V>
 */
public class AccStateFragmentBody<I, E, K, V>
    extends AccStateMemberSet<I, E, K, V>
{
    protected AccStateFragmentBody(Map<Object, Integer> fieldIdToIndex, AccStateTypeTransition<I, E, K, V>[] edgeAccs) {
        super(fieldIdToIndex, edgeAccs);
    }

    @Override
    public GonType getGonType() {
        return GonType.ENTRY;
    }

    /** Create a new instance and set it as the parent on all the property accumulators */
    public static <I, E, K, V> AccStateFragmentBody<I, E, K, V> of(Map<Object, Integer> stateIdToIndex, AccStateTypeTransition<I, E, K, V>[] edgeAccs) {
        AccStateFragmentBody<I, E, K, V> result = new AccStateFragmentBody<>(stateIdToIndex, edgeAccs);
        for (AccStateTypeTransition<I, E, K, V> acc : edgeAccs) {
            acc.setParent(result);
        }
        return result;
    }

    @Override
    public void beginActual() throws IOException {
        // Reset fields
        currentFieldIndex = -1;
        currentFieldAcc = null;
    }

    @Override
    public void endActual() throws IOException {
        // Visit all remaining fields
        for (int i = currentFieldIndex + 1; i < edgeAccs.length; ++i) {
            // AccStateTypeProduceEntry<I, E, K, V> edgeAcc = edgeAccs[i];
            AccStateGon<I, E, K, V> edgeAcc = edgeAccs[i];
            // Edge.begin receives the target of an edge - but there is none so we pass null

            // With these calls we tell the fields that there is no value
            edgeAcc.begin(null, null, null, skipOutput); // TODO Passing 'null' as the start node to indicate absent values is perhaps not the best API contract
            edgeAcc.end();
        }

        currentFieldIndex = -1;
        currentFieldAcc = null;
    }

    @Override
    public String toString() {
        return "AccStateFragmentBody (source: " + currentSourceNode + ", field: " + currentFieldAcc + ", members: " + Arrays.asList(edgeAccs) + ")";
    }
}

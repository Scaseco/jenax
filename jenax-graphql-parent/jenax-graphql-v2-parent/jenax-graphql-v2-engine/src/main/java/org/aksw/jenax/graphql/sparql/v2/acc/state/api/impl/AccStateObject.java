package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateMemberSet;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeProduceObject;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeTransition;
import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProvider;

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
public class AccStateObject<I, E, K, V>
    extends AccStateMemberSet<I, E, K, V>
    implements AccStateTypeProduceObject<I, E, K, V>
{
    //protected BiFunction<I, E, ?> inputToObjectId;
    protected Object value;
    protected boolean isArray;

    /** Should not be used directly; use a builder. */
//    public AccStateObject() {
//        this(BiFunction<I, E, ?> inputToObjectId, new HashMap<>(), new AccLateralStructMember[0]);
//    }

    // BiFunction<I, E, ?> inputToObjectId,
    protected AccStateObject(Map<Object, Integer> fieldIdToIndex, AccStateTypeTransition<I, E, K, V>[] edgeAccs, boolean isArray) {
        super(fieldIdToIndex, edgeAccs);
        this.isArray = isArray;
    }

//    @Override
//    public AccStateTypeNonObject<I, E, K, V> getParent() {
//        return (AccStateTypeNonObject<I, E, K, V>)parent;
//    }

//    @Override
//    public void setParent(AccLateralTypeOb<I, E, K, V> parent) {
//        // TODO Auto-generated method stub
//        super.setParent(parent);
//    }

    /** Create a new instance and set it as the parent on all the property accumulators */
    public static <I, E, K, V> AccStateObject<I, E, K, V> of(boolean isArray, Map<Object, Integer> stateIdToIndex, AccStateTypeTransition<I, E, K, V>[] edgeAccs) {
        AccStateObject<I, E, K, V> result = new AccStateObject<>(stateIdToIndex, edgeAccs, isArray);
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

        if (!skipOutput) {
            if (context.isMaterialize()) {
                GonProvider<K, V> gonProvider = context.getGonProvider();
                value = gonProvider.newObject();
                // FIXME value = new RdfObjectImpl(source);
            }

            if (context.isSerialize()) {
                if (!isArray) {
                    context.getJsonWriter().beginObject();
                } else {
                    // The array wrapping is created by the referencing property.
                    // context.getJsonWriter().beginArray();
                }
            }
        }
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

        if (!skipOutput) {
            if (context.isMaterialize() && parent != null) {
                // FIXME parent.acceptContribution(value, context);
            }

            if (context.isSerialize()) {
                if (!isArray) {
                    context.getJsonWriter().endObject();
                } else {
                    // context.getJsonWriter().endArray();
                }
            }
        }

        currentFieldIndex = -1;
        currentFieldAcc = null;
    }

    @Override
    public String toString() {
        return "AccStateObject (source: " + currentSourceNode + ", field: " + currentFieldAcc + ", members: " + Arrays.asList(edgeAccs) + ")";
    }
}

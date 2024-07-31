package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeProduceEntryBase;
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
public class AccStateFragmentHead<I, E, K, V>
    // extends AccStateMemberSet<I, E, K, V>
    extends  AccStateTypeProduceEntryBase<I, E, K, V>
{
    //protected BiFunction<I, E, ?> inputToObjectId;
    // protected Object value;

    /** Should not be used directly; use a builder. */
//    public AccStateObject() {
//        this(BiFunction<I, E, ?> inputToObjectId, new HashMap<>(), new AccLateralStructMember[0]);
//    }
    protected Object matchStateId;
    protected AccStateGon<I, E, K, V> fragmentBody;

    // BiFunction<I, E, ?> inputToObjectId,
    protected AccStateFragmentHead(Object matchStateId, AccStateGon<I, E, K, V> fragmentBody) {
        // super(matchStateId);
        this.matchStateId = matchStateId;
        this.fragmentBody = fragmentBody;
    }

    @Override
    public GonType getGonType() {
        return GonType.ENTRY;
    }

    @Override
    public Object getMatchStateId() {
        return matchStateId;
    }

//    @Override
//    public void setParent(AccLateralTypeOb<I, E, K, V> parent) {
//        // TODO Auto-generated method stub
//        super.setParent(parent);
//    }

    /** Create a new instance and set it as the parent on all the property accumulators */
    public static <I, E, K, V> AccStateFragmentHead<I, E, K, V> of(Object matchStateId, AccStateGon<I, E, K, V> fragmentBody) {
        AccStateFragmentHead<I, E, K, V> result = new AccStateFragmentHead<>(matchStateId, fragmentBody);
        fragmentBody.setParent(result);
//        for (AccStateTypeTransition<I, E, K, V> acc : edgeAccs) {
//            acc.setParent(result);
//        }
        return result;
    }

//    @Override
//    public void beginActual() throws IOException {
        // Reset fields
//        currentFieldIndex = -1;
//        currentFieldAcc = null;

//        if (!skipOutput) {
//            if (context.isMaterialize()) {
//                GonProvider<K, V> gonProvider = context.getGonProvider();
//                value = gonProvider.newObject();
//                // FIXME value = new RdfObjectImpl(source);
//            }
//
//            if (context.isSerialize()) {
//                context.getJsonWriter().beginObject();
//            }
//        }
//    }

    @Override
    public final AccStateGon<I, E, K, V> transitionActual(Object inputStateId, I input, E env) throws IOException {
        AccStateGon<I, E, K, V> result;
        if (Objects.equals(inputStateId, matchStateId)) {
            fragmentBody.begin(inputStateId, input, env, skipOutput);
            result = fragmentBody;
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public void endActual() throws IOException {
        if (fragmentBody.hasBegun()) {
            fragmentBody.end();
        }

        // Visit all remaining fields
//        for (int i = currentFieldIndex + 1; i < edgeAccs.length; ++i) {
//            // AccStateTypeProduceEntry<I, E, K, V> edgeAcc = edgeAccs[i];
//            AccStateGon<I, E, K, V> edgeAcc = edgeAccs[i];
//            // Edge.begin receives the target of an edge - but there is none so we pass null
//
//            // With these calls we tell the fields that there is no value
//            edgeAcc.begin(null, null, null, skipOutput); // TODO Passing 'null' as the start node to indicate absent values is perhaps not the best API contract
//            edgeAcc.end();
//        }
//
//        if (!skipOutput) {
//            if (context.isMaterialize() && parent != null) {
//                // FIXME parent.acceptContribution(value, context);
//            }
//
//            if (context.isSerialize()) {
//                context.getJsonWriter().endObject();
//            }
//        }

//        currentFieldIndex = -1;
//        currentFieldAcc = null;
    }

    @Override
    public String toString() {
        return "AccStateFragmentHead (matches: " + matchStateId + ", field: " + currentInput + ", body: " + fragmentBody + ")";
    }

    @Override
    public Iterator<? extends AccStateGon<I, E, K, V>> children() {
        return List.of(fragmentBody).iterator();
    }
}

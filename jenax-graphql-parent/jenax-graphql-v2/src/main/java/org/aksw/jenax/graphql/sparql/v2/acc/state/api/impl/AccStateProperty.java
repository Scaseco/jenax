package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccJsonErrorHandler;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProvider;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;

public class AccStateProperty<I, E, K, V>
    extends AccStatePropertyBase<I, E, K, V>
{
    // protected Node currentTarget = null;
    // protected AccStateTypeProduceNode<I, E, K, V> targetAcc;
    protected AccStateGon<I, E, K, V> targetAcc;
    protected boolean skipOutputStartedHere = false;

    // since last call to begin()
    protected long seenTargetCount = 0;

     /** The current materialized value */
    protected Object value;

    // public AccJsonProperty(TupleBridge3<Binding, Node> tripleAccessor, P_Path0 jsonKey, Node matchFieldId, boolean isForward, AccJsonNode targetAcc, boolean isSingle) {
    // public AccStateProperty(Object matchStateId, K memberKey, AccStateTypeProduceNode<I, E, K, V> targetAcc, boolean isSingle) {
    public AccStateProperty(Object matchStateId, K memberKey, AccStateGon<I, E, K, V> targetAcc, boolean isSingle) {
        super(matchStateId, memberKey, isSingle);
        this.targetAcc = targetAcc;
    }

//    @Override
//    public PathJson getPath() {
//        return (parent != null ? parent.getPath() : PathJson.newRelativePath()).resolve(Step.of((int)seenTargetCount));
//    }


//    @Override
//    public void setTargetAcc(AccJsonNode targetAcc) {
//        targetAcc.setParent(this);
//        this.targetAcc = targetAcc;
//    }

    /**
     * Sets the source node which subsequent triples must match in addition to the fieldId.
     * This method should be called by the owner of the edge such as AccJsonObject.
     * @throws IOException
     */
    @Override
    public void beginActual() throws IOException {
        seenTargetCount = 0;
        skipOutputStartedHere = false;

        if (!skipOutput) {
            if (context.isMaterialize()) {
                GonProvider<K, V> gonProvider = context.getGonProvider();

                value = isSingle
                        ? gonProvider.newNull()
                        : gonProvider.newArray();
            }

            if (context.isSerialize()) {
                ObjectNotationWriter<K, V> writer = context.getJsonWriter();
                writer.name(memberKey);
                if (!isSingle) {
                    writer.beginArray();
                }
            }
        }
    }

    /** Accepts a triple if source and field id match that of the current state */
    @Override
    public AccStateGon<I, E, K, V> transitionActual(Object inputStateId, I input, E env) throws IOException {
        // AccStateTypeProduceNode<I, E, K, V> result = null;
        AccStateGon<I, E, K, V> result = null;
        // Object inputStateId = getInputStateId(input, env);
        if (Objects.equals(matchStateId, inputStateId)) {
            // if (Objects.equals(currentSourceNode, edgeInputSource)) {
                ++seenTargetCount;
                // System.err.println("items so far seen at path " + getPath() + ": " + seenTargetCount);

                // If there is too many items we need still consume all the edges as usual
                // but we call begin() on the accumulators with serialization disabled
                boolean isTooMany = isSingle && seenTargetCount > 1;
                if (isTooMany) {
                    this.skipOutputStartedHere = true;
                    AccJsonErrorHandler errorHandler = context.getErrorHandler();
                    if (errorHandler != null) {
                        throw new RuntimeException("Error handler not yet implemented");
                        // PathJson path = getPath();
                        // errorHandler.accept(new AccJsonErrorEvent(path, "Multiple values encountered for a field that was declared to have at most a single one."));
                    }
                }

               //  currentTarget = isForward ? o : s; // TripleUtils.getTarget(input, isForward);
                targetAcc.begin(null, input, env, skipOutput || skipOutputStartedHere);
                result = targetAcc;
            // }
        }
        return result;
    }

    @Override
    public void endActual() throws IOException {
        if (!skipOutput) {
            if (context.isMaterialize()) {
                GonProvider<K, V> gonProvider = context.getGonProvider();
                // XXX Calling parent.getValue() here causes IllegalState because
                // getValue() must only be called after end()
                // So we access the field directly
                if (parent != null) {
                    // Turns null into JsonNull
                    Object elt = value == null ? gonProvider.newNull() : value;
                    // AccJsonObjectLikeBase acc = (AccJsonObjectLikeBase)parent;

                    // TODO Can we have the parent access the value in the child rather than the child accessing the parent?
                    // AccStateTypeProduceObject
                    AccStateGon<I, E, K, V> valueParent = getParent();
                    throw new RuntimeException("materialization no longer supported");
                    // Object parentValue = valueParent.getValue();
                    // parentAcc.value.getAsObject().getMembers().put(memberKey, elt);
                    // gonProvider.setProperty(parentValue, memberKey, elt);
                }
            }

            if (context.isSerialize()) {
                ObjectNotationWriter<K, V> jsonWriter = context.getJsonWriter();
                if (!isSingle) {
                    jsonWriter.endArray();
                } else if (seenTargetCount == 0) {
                    jsonWriter.nullValue();
                }
            }
        }
    }

    @Override
    public String toString() {
        return "AccStateProperty(matches: " + matchStateId + ", currentInput: " + currentInput + ", " + targetAcc + ")";
    }

    @Override
    public Iterator<? extends AccStateGon<I, E, K, V>> children() {
        return List.of(targetAcc).iterator();
    }

//    @Override
//    public void acceptContribution(RdfElement item, AccContextRdf context) {
//        ensureBegun();
//        if (!skipOutput) {
//            if (context.isMaterialize()) {
//                if (isSingle) {
//                    if (value == null) {
//                        value = item;
//                    } else {
//                        // TODO Report an error, ignore or overwrite?
//                    }
//                } else {
//                    value.getAsArray().add(item);
//                }
//            }
//        }
//    }
}


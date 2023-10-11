package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;
import java.util.Objects;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.path.json.PathJson.Step;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonWriter;

// TODO Should we model edges as a top-level state? or is the active edge a state within the parent JsonObject node?
class AccJsonEdgeImpl
    extends AccJsonBase
    implements AccJsonEdge
{
    protected String matchFieldId; // AccJsonObject should index AccJsonEdge by this attribute
    protected boolean isForward;

    protected String jsonKey;

    protected Node currentTarget = null;
    protected AccJsonNode targetAcc;

    // since last call to begin()
    protected long seenTargetCount = 0;

    /** If true then no array is created. Any item after the first raises an error event. */
    protected boolean isSingle;

    public AccJsonEdgeImpl(String jsonKey, String matchFieldId, boolean isForward) {
        super();
        this.matchFieldId = matchFieldId;
        this.jsonKey = jsonKey;
        this.isForward = isForward;
    }

    @Override
    public PathJson getPath() {
        return (parent != null ? parent.getPath() : PathJson.newRelativePath()).resolve(Step.of((int)seenTargetCount));
    }

    @Override
    public void setSingle(boolean isSingle) {
        this.isSingle = isSingle;
    }

    @Override
    public boolean isSingle() {
        return this.isSingle;
    }

    @Override
    public void setTargetAcc(AccJsonNode targetAcc) {
        Preconditions.checkArgument(targetAcc.getParent() == null, "Parent already set");
        targetAcc.setParent(this);
        this.targetAcc = targetAcc;
    }

    @Override
    public String getJsonKey() {
        return jsonKey;
    }

    @Override
    public String getMatchFieldId() {
        return matchFieldId;
    }

    @Override
    public boolean isForward() {
        return isForward;
    }

    /**
     * Sets the source node which subsequent triples must match in addition to the fieldId.
     * This method should be called by the owner of the edge such as AccJsonObject.
     * @throws IOException
     */
    @Override
    public void begin(Node node, AccContext context, boolean skipOutput) throws Exception {
        super.begin(node, context, skipOutput);

        seenTargetCount = 0;

        if (context.isMaterialize()) {
            value = new JsonArray();
        }

        if (!skipOutput && context.isSerialize()) {
            JsonWriter jsonWriter = context.getJsonWriter();
            jsonWriter.name(jsonKey);
            if (!isSingle) {
                jsonWriter.beginArray();
            }
        }
    }

    @Override
    public void end(AccContext context) throws Exception {
        if (!hasBegun()) { // FIXME hash - we should not allow end without begin!
            return;
        }
        endCurrentTarget(context);

        if (!skipOutput && context.isSerialize()) {
            JsonWriter jsonWriter = context.getJsonWriter();
            if (!isSingle) {
                jsonWriter.endArray();
            }
        }

        super.end(context);
    }

    protected void endCurrentTarget(AccContext context) throws Exception {
        if (currentTarget != null) {
            targetAcc.end(context);

            if (context.isMaterialize()) {
                this.value.getAsJsonArray().add(targetAcc.getValue());
            }

            currentTarget = null;
        }
    }

    /** Accepts a triple if source and field id match that of the current state */
    @Override
    public AccJson transition(Triple input, AccContext context) throws Exception {
        AccJson result = null;
        String inputFieldId = input.getPredicate().getURI();
        if (matchFieldId.equals(inputFieldId)) {
            Node edgeInputSource = TripleUtils.getSource(input, isForward);

            endCurrentTarget(context);

            if (Objects.equals(currentSourceNode, edgeInputSource)) {
                ++seenTargetCount;
                System.err.println("items so far seen at path " + getPath() + ": " + seenTargetCount);
                boolean isTooMany = isSingle && seenTargetCount > 1;
                if (isTooMany) {
                    AccJsonErrorHandler errorHandler = context.getErrorHandler();
                    if (errorHandler != null) {
                        PathJson path = getPath();
                        errorHandler.accept(new AccJsonErrorEvent(path, "Multiple values encountered for a field that was declared to have at most a single one."));
                        this.skipOutput = true;
                    }

                }

                // FIXME If there is too many items we need still need to do pass all the edges to the
                // accumulators but we need to disable serialization!

                // How to disable that? Using the context?

                currentTarget = TripleUtils.getTarget(input, isForward);
                targetAcc.begin(currentTarget, context, skipOutput);
                result = targetAcc;


                // result = targetAcc.transition(input, context);
            } else {
                // I think we don't have to end the target acc here, because
                // the parent will close it since we indicate we don't have a transition
//                if (currentSourceNode != null) {
//                    targetAcc.end(context);
//
//                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Field(matches: " + matchFieldId + ", target: " + currentTarget + ", " + targetAcc + ")";
    }
}

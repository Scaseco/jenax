package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;
import java.util.Objects;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.path.json.PathJson.Step;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.stream.JsonWriter;

// TODO Should we model edges as a top-level state? or is the active edge a state within the parent JsonObject node?
public class AccJsonProperty
    extends AccJsonBase
    implements AccJsonEdge
{
    protected Node matchFieldId; // AccJsonObject should index AccJsonEdge by this attribute
    protected boolean isForward;

    protected String jsonKey;

    protected Node currentTarget = null;
    protected AccJsonNode targetAcc;
    protected boolean skipOutputStartedHere = false;

    // since last call to begin()
    protected long seenTargetCount = 0;

    /** If true then no array is created. Any item after the first raises an error event. */
    protected boolean isSingle = false;

    public AccJsonProperty(String jsonKey, Node matchFieldId, boolean isForward, AccJsonNode targetAcc) {
        super();
        this.matchFieldId = matchFieldId;
        this.jsonKey = jsonKey;
        this.isForward = isForward;
        this.targetAcc = targetAcc;
    }

    @Override
    public PathJson getPath() {
        return (parent != null ? parent.getPath() : PathJson.newRelativePath()).resolve(Step.of((int)seenTargetCount));
    }

    @Override
    public void setSingle(boolean value) {
        this.isSingle = value;
    }

    @Override
    public boolean isSingle() {
        return this.isSingle;
    }

    @Override
    public void setTargetAcc(AccJsonNode targetAcc) {
        targetAcc.setParent(this);
        this.targetAcc = targetAcc;
    }

    @Override
    public String getJsonKey() {
        return jsonKey;
    }

    @Override
    public Node getMatchFieldId() {
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
    public void begin(Node node, AccContext context, boolean skipOutput) throws IOException {
        super.begin(node, context, skipOutput);
        seenTargetCount = 0;
        skipOutputStartedHere = false;

        if (!skipOutput) {
            if (context.isMaterialize()) {
                value = isSingle
                        ? null
                        : new JsonArray();
            }

            if (context.isSerialize()) {
                JsonWriter jsonWriter = context.getJsonWriter();
                jsonWriter.name(jsonKey);
                if (!isSingle) {
                    jsonWriter.beginArray();
                }
            }
        }
    }

    /** Accepts a triple if source and field id match that of the current state */
    @Override
    public AccJson transition(Triple input, AccContext context) throws IOException {
        ensureBegun();

        // End the current target (array item) if there is one
        // endCurrentTarget(context);

        AccJson result = null;
        Node inputFieldId = input.getPredicate(); //.getURI();
        if (matchFieldId.equals(inputFieldId)) {
            Node edgeInputSource = TripleUtils.getSource(input, isForward);

            // endCurrentTarget(context);

            if (Objects.equals(currentSourceNode, edgeInputSource)) {
                ++seenTargetCount;
                // System.err.println("items so far seen at path " + getPath() + ": " + seenTargetCount);

                // If there is too many items we need still consume all the edges as usual
                // but we call begin() on the accumulators with serialization disabled
                boolean isTooMany = isSingle && seenTargetCount > 1;
                if (isTooMany) {
                    AccJsonErrorHandler errorHandler = context.getErrorHandler();
                    if (errorHandler != null) {
                        PathJson path = getPath();
                        errorHandler.accept(new AccJsonErrorEvent(path, "Multiple values encountered for a field that was declared to have at most a single one."));
                        this.skipOutputStartedHere = true;
                    }
                }

                currentTarget = TripleUtils.getTarget(input, isForward);
                targetAcc.begin(currentTarget, context, skipOutput || skipOutputStartedHere);
                result = targetAcc;
            }
        }
        return result;
    }

    @Override
    public void end(AccContext context) throws IOException {
        ensureBegun();

        if (!skipOutput) {
            if (context.isMaterialize()) {
                // XXX Calling parent.getValue() here causes IllegalState because
                // getValue() must only be called after end()
                // So we access the field directly
                if (parent != null) {
                    // Turns null into JsonNull
                    JsonElement elt = value == null ? JsonNull.INSTANCE : value;
                    AccJsonObject acc = (AccJsonObject)parent;
                    acc.value.getAsJsonObject().add(jsonKey, elt);
                }
            }

            if (context.isSerialize()) {
                JsonWriter jsonWriter = context.getJsonWriter();
                if (!isSingle) {
                    jsonWriter.endArray();
                } else if (seenTargetCount == 0) {
                    jsonWriter.nullValue();
                }
            }
        }
        super.end(context);
    }

    @Override
    public String toString() {
        return "Field(matches: " + matchFieldId + ", target: " + currentTarget + ", " + targetAcc + ")";
    }

    @Override
    public void acceptContribution(JsonElement item, AccContext context) {
        ensureBegun();
        if (!skipOutput) {
            if (context.isMaterialize()) {
                if (isSingle) {
                    if (value == null) {
                        value = item;
                    } else {
                        // error
                    }
                } else {
                    value.getAsJsonArray().add(item);
                }
            }
        }
    }
}

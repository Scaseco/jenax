package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;
import java.util.Objects;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.path.json.PathJson.Step;
import org.aksw.jenax.io.json.writer.RdfObjectNotationWriter;
import org.aksw.jenax.ron.RdfArrayImpl;
import org.aksw.jenax.ron.RdfElement;
import org.aksw.jenax.ron.RdfNull;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.path.P_Path0;

// TODO Clarify the following question, the current model should be fairly stable by now: Should we model edges as a top-level state? or is the active edge a state within the parent JsonObject node?
/**
 * Accumulator for the values of an (objectId, propertyId) pair.
 */
public class AccJsonProperty
    extends AccJsonBase
    implements AccJsonEdge
{
    // protected TupleBridge3<Binding, Node> tripleAccessor;

    protected Node matchFieldId; // AccJsonObject should index AccJsonEdge by this attribute
    protected boolean isForward;

    /** The property that is emitted by this edge accumulator if values are encountered. */
    protected P_Path0 jsonKey;

    protected Node currentTarget = null;
    protected AccJsonNode targetAcc;
    protected boolean skipOutputStartedHere = false;

    // since last call to begin()
    protected long seenTargetCount = 0;

    /** If true then no array is created. Any item after the first raises an error event. */
    protected boolean isSingle = false;

    // public AccJsonProperty(TupleBridge3<Binding, Node> tripleAccessor, P_Path0 jsonKey, Node matchFieldId, boolean isForward, AccJsonNode targetAcc, boolean isSingle) {
    public AccJsonProperty(P_Path0 jsonKey, Node matchFieldId, boolean isForward, AccJsonNode targetAcc, boolean isSingle) {
        super();
        this.matchFieldId = matchFieldId;
        this.jsonKey = jsonKey;
        this.isForward = isForward;
        this.targetAcc = targetAcc;
        this.isSingle = isSingle;
    }

    @Override
    public PathJson getPath() {
        return (parent != null ? parent.getPath() : PathJson.newRelativePath()).resolve(Step.of((int)seenTargetCount));
    }

//    @Override
//    public void setSingle(boolean value) {
//        this.isSingle = value;
//    }

    // @Override
    public boolean isSingle() {
        return this.isSingle;
    }

//    @Override
//    public void setTargetAcc(AccJsonNode targetAcc) {
//        targetAcc.setParent(this);
//        this.targetAcc = targetAcc;
//    }

    @Override
    public P_Path0 getJsonKey() {
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
    public void begin(Node node, AccContextRdf context, boolean skipOutput) throws IOException {
        super.begin(node, context, skipOutput);
        seenTargetCount = 0;
        skipOutputStartedHere = false;

        if (!skipOutput) {
            if (context.isMaterialize()) {
                value = isSingle
                        ? null
                        : new RdfArrayImpl(); // new JsonArray();
            }

            if (context.isSerialize()) {
                RdfObjectNotationWriter writer = context.getJsonWriter();
                writer.name(jsonKey);
                if (!isSingle) {
                    writer.beginArray();
                }
            }
        }
    }

    /** Accepts a triple if source and field id match that of the current state */
    @Override
    public AccJson transition(Triple input, AccContextRdf context) throws IOException {
        ensureBegun();

//        Node s = tripleAccessor.getArg0(input);
//        Node p = tripleAccessor.getArg1(input);
//        Node o = tripleAccessor.getArg2(input);

        Node s = input.getSubject();
        Node p = input.getPredicate();
        Node o = input.getObject();

        // End the current target (array item) if there is one
        // endCurrentTarget(context);

        AccJson result = null;
        Node inputFieldId = p; //.getURI();
        if (matchFieldId.equals(inputFieldId)) {
            Node edgeInputSource = isForward ? s : o; // TripleUtils.getSource(input, isForward);

            // endCurrentTarget(context);

            if (Objects.equals(currentSourceNode, edgeInputSource)) {
                ++seenTargetCount;
                // System.err.println("items so far seen at path " + getPath() + ": " + seenTargetCount);

                // If there is too many items we need still consume all the edges as usual
                // but we call begin() on the accumulators with serialization disabled
                boolean isTooMany = isSingle && seenTargetCount > 1;
                if (isTooMany) {
                    this.skipOutputStartedHere = true;
                    AccJsonErrorHandler errorHandler = context.getErrorHandler();
                    if (errorHandler != null) {
                        PathJson path = getPath();
                        errorHandler.accept(new AccJsonErrorEvent(path, "Multiple values encountered for a field that was declared to have at most a single one."));
                    }
                }

                currentTarget = isForward ? o : s; // TripleUtils.getTarget(input, isForward);
                targetAcc.begin(currentTarget, context, skipOutput || skipOutputStartedHere);
                result = targetAcc;
            }
        }
        return result;
    }

    @Override
    public void end(AccContextRdf context) throws IOException {
        ensureBegun();

        if (!skipOutput) {
            if (context.isMaterialize()) {
                // XXX Calling parent.getValue() here causes IllegalState because
                // getValue() must only be called after end()
                // So we access the field directly
                if (parent != null) {
                    // Turns null into JsonNull
                    RdfElement elt = value == null ? new RdfNull() : value;
                    AccJsonObjectLikeBase acc = (AccJsonObjectLikeBase)parent;
                    acc.value.getAsObject().getMembers().put(jsonKey, elt);
                }
            }

            if (context.isSerialize()) {
                RdfObjectNotationWriter jsonWriter = context.getJsonWriter();
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
    public void acceptContribution(RdfElement item, AccContextRdf context) {
        ensureBegun();
        if (!skipOutput) {
            if (context.isMaterialize()) {
                if (isSingle) {
                    if (value == null) {
                        value = item;
                    } else {
                        // TODO Report an error, ignore or overwrite?
                    }
                } else {
                    value.getAsArray().add(item);
                }
            }
        }
    }
}

package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;
import java.util.Objects;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.path.json.PathJson.Step;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.io.rdf.json.RdfElement;
import org.aksw.jenax.io.rdf.json.RdfObject;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * An accumulator for a conditional transition to the fragment body.
 */
public class AccJsonFragmentHead
    extends AccJsonBase
    implements AccJsonEdge
{
    protected Node matchFieldId; // AccJsonObject should index AccJsonEdge by this attribute
    protected boolean isForward;

    protected Node dummyJsonKey;

    protected Node currentTarget = null;
    protected AccJsonNode targetAcc;
    protected boolean skipOutputStartedHere = false;

    // since last call to begin()
    protected long seenTargetCount = 0;

    /** If true then no array is created. Any item after the first raises an error event. */
    // protected boolean isSingle = false;

    public AccJsonFragmentHead(Node dummyJsonKey, Node matchFieldId, boolean isForward, AccJsonNode targetAcc) {
        super();
        this.matchFieldId = matchFieldId;
        this.isForward = isForward;
        this.dummyJsonKey = dummyJsonKey;
        this.targetAcc = targetAcc;
    }

    @Override
    public PathJson getPath() {
        return (parent != null ? parent.getPath() : PathJson.newRelativePath()).resolve(Step.of((int)seenTargetCount));
    }

//    @Override
//    public void setSingle(boolean value) {
//        this.isSingle = value;
//    }
//
//    @Override
//    public boolean isSingle() {
//        return this.isSingle;
//    }

//    @Override
//    public void setTargetAcc(AccJsonNode targetAcc) {
//        targetAcc.setParent(this);
//        this.targetAcc = targetAcc;
//    }

    @Override
    public Node getJsonKey() {
        return dummyJsonKey;
    }

    @Override
    public Node getMatchFieldId() {
        return matchFieldId;
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
                // Create a temporary object
                value = new RdfObject(dummyJsonKey);// parent.getValue();
//                value = isSingle
//                        ? null
//                        : RdfElement.newArray(); // new JsonArray();
            }

//            if (context.isSerialize()) {
//                RdfObjectNotationWriter writer = context.getJsonWriter();
                // writer.name(jsonKey);
//                if (!isSingle) {
//                    writer.beginArray();
//                }
//            }
        }
    }

    /** Accepts a triple if source and field id match that of the current state */
    @Override
    public AccJson transition(Triple input, AccContext context) throws IOException {
        ensureBegun();

        // End the current target (array item) if there is one
        // endCurrentTarget(context);

        AccJson result = null;
        Node inputFieldId = input.getPredicate();
        if (matchFieldId.equals(inputFieldId)) {
            Node edgeInputSource = TripleUtils.getSource(input, true);

            // endCurrentTarget(context);

            if (Objects.equals(currentSourceNode, edgeInputSource)) {
                ++seenTargetCount;
                // System.err.println("items so far seen at path " + getPath() + ": " + seenTargetCount);

                // If there is too many items we need still consume all the edges as usual
                // but we call begin() on the accumulators with serialization disabled
//                boolean isTooMany = isSingle && seenTargetCount > 1;
//                if (isTooMany) {
//                    this.skipOutputStartedHere = true;
//                    AccJsonErrorHandler errorHandler = context.getErrorHandler();
//                    if (errorHandler != null) {
//                        PathJson path = getPath();
//                        errorHandler.accept(new AccJsonErrorEvent(path, "Multiple values encountered for a field that was declared to have at most a single one."));
//                    }
//                }

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
                    RdfElement elt = value == null ? RdfElement.nullValue() : value;

                    // parent can be either Object or Fragment.
                    AccJson tmp = parent;
                    while (true) {
                        if (tmp instanceof AccJsonObject o) {
                            break;
                        } else if (parent instanceof AccJsonFragmentHead f) {
                            tmp = f.parent;
                        }
                    }

                    AccJsonObject parentAcc = (AccJsonObject)tmp;
                    boolean showFragments = false;
                    if (showFragments) {
                        // Debug: Expose the fragment in the parent object under the dummyJsonKey
                        parentAcc.value.getAsObject().add(dummyJsonKey, elt);
                    } else {
                        parentAcc.value.getAsObject().getMembers().putAll(elt.getAsObject().getMembers());
                    }
                }
            }

//            if (context.isSerialize()) {
//                StructuredWriterRdf jsonWriter = context.getJsonWriter();
//                if (!isSingle) {
//                    jsonWriter.endArray();
//                } else if (seenTargetCount == 0) {
//                    jsonWriter.nullValue();
//                }
//            }
        }
        super.end(context);
    }

    @Override
    public String toString() {
        return "Field(matches: " + matchFieldId + ", target: " + currentTarget + ", " + targetAcc + ")";
    }

    @Override
    public void acceptContribution(RdfElement item, AccContext context) {
        ensureBegun();
        if (!skipOutput) {
            if (context.isMaterialize()) {
                // value.getAsArray().add(item);
                RdfObject src = item.getAsObject();
                RdfObject tgt = value.getAsObject();
                tgt.getMembers().putAll(src.getMembers());
            }
        }
    }

    @Override
    public boolean isForward() {
        return isForward;
    }

}

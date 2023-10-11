package org.aksw.jenax.io.json.accumulator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.path.json.PathJson.Step;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class AccJsonNodeObject
    extends AccJsonBase
    implements AccJsonNode
{

    @Override
    public PathJson getPath() {
        String stepName = currentFieldAcc == null ? "(no active field)" : currentFieldAcc.getJsonKey();
        return (parent != null ? parent.getPath() : PathJson.newRelativePath()).resolve(Step.of(stepName));
    }

//    public AccJsonNodeObject(AccJson parent) {
//        super(parent);
//    }

    protected Map<String, Integer> fieldIdToIndex = new HashMap<>();
    protected AccJsonEdge[] edgeAccs = new AccJsonEdge[0];

    protected int currentFieldIndex = -1;
    protected AccJsonEdge currentFieldAcc = null;

    // If a root node sees a new source then the start of a new result entity is assumed
    // If a non-root node sees a new source then it rejects it
    protected boolean isRootNode = false;

    @Override
    public void begin(Node source, AccContext context, boolean skipOutput) throws Exception {
        super.begin(source, context, skipOutput);

        // Reset fields
        currentFieldIndex = -1;
        currentFieldAcc = null;

        if (!skipOutput && context.isMaterialize()) {
            value = new JsonObject();
        }
        if (!skipOutput && context.isSerialize()) {
            context.getJsonWriter().beginObject();
        }
    }

    @Override
    public void end(AccContext context) throws Exception {
        if (!hasBegun()) { // FIXME hash - we should not allow end without begin!
            return;
        }
        // end the current field (if any)
        if (currentFieldAcc != null) {
            currentFieldAcc.end(context);

            if (context.isMaterialize()) {
                String jsonKey = currentFieldAcc.getJsonKey();
                JsonElement fieldValue = currentFieldAcc.getValue();
                value.getAsJsonObject().add(jsonKey, fieldValue);
            }

        }

        // Visit all remaining fields
        for (int i = currentFieldIndex + 1; i < edgeAccs.length; ++i) {
            AccJsonEdge edgeAcc = edgeAccs[i];
            edgeAcc.begin(null, context, skipOutput); // TODO We need to tell fields that there is no value
            edgeAcc.end(context);

            String fieldName = edgeAcc.getJsonKey();
            JsonElement fieldValue = edgeAcc.getValue();
            if (context.isMaterialize()) {
                value.getAsJsonObject().add(fieldName, fieldValue);
            }
        }

        if (!skipOutput && context.isSerialize()) {
            context.getJsonWriter().endObject();
        }

        currentFieldIndex = -1;
        currentFieldAcc = null;
        super.end(context);
    }

    @Override
    public AccJson transition(Triple input, AccContext context) throws Exception {

        String inputFieldId = input.getPredicate().getURI();

        AccJson result = null;
        Integer inputFieldIndex = fieldIdToIndex.get(inputFieldId);
        if (inputFieldIndex == null) {
            // No such field - reject
            // result = null;
        } else {
            // Visit all accs between the current index and the matching one
            int requestedFieldIndex = inputFieldIndex.intValue();

            if (requestedFieldIndex != currentFieldIndex) {
                // End the current field (if not null)
                if (currentFieldAcc != null) {
                    currentFieldAcc.end(context);
                }

                // The requested field comes before the current field
                // This should only happen if there is a new source
                // Sanity check: Check that the source of this field is different from the current sourceNode
                if (requestedFieldIndex < currentFieldIndex) {
                    AccJsonEdge edgeAcc = edgeAccs[requestedFieldIndex];
                    Node inputSource = TripleUtils.getSource(input, edgeAcc.isForward());
                    if (Objects.equals(inputSource, currentSourceNode)) {
                        throw new RuntimeException("fields appear to have arrived out of order - should not happen");
                        // TODO Improve error message: on sourceNode data for field [] was expected to arrive after field []
                    }
                }

                // Skip over the remaining fields
                for (int i = currentFieldIndex + 1; i < requestedFieldIndex; ++i) {
                    AccJsonEdge edgeAcc = edgeAccs[i];
                    edgeAcc.begin(null, context, skipOutput);
                    edgeAcc.end(context);

                    if (context.isMaterialize()) {
                        String jsonKey = edgeAcc.getJsonKey();
                        value.getAsJsonObject().add(jsonKey, edgeAcc.getValue());
                    }
                }

                currentFieldIndex = requestedFieldIndex;
                currentFieldAcc = edgeAccs[requestedFieldIndex];

                boolean isForward = currentFieldAcc.isForward();
                // Node target = TripleUtils.getTarget(input, isForward);
                Node edgeInputSource = TripleUtils.getSource(input, isForward);

                if (!Objects.equals(edgeInputSource, currentSourceNode)) {
                    throw new RuntimeException("should not happen - node at " + currentSourceNode + " but edge claims " + edgeInputSource);
                }

                currentFieldAcc.begin(edgeInputSource, context, skipOutput);
                result = currentFieldAcc.transition(input, context);
            }
        }
        return result;
    }

    public void addEdge(AccJsonEdge subAcc) {
        // TODO Lots of array copying!
        // We should add a builder for efficiet adds and derive the more efficient array version from it.
        String fieldId = subAcc.getMatchFieldId();
        int fieldIndex = edgeAccs.length;
        fieldIdToIndex.put(fieldId, fieldIndex);
        edgeAccs = Arrays.copyOf(edgeAccs, fieldIndex + 1);
        edgeAccs[fieldIndex] = subAcc;
        subAcc.setParent(this);
    }

    @Override
    public String toString() {
        return "AccJsonNodeObject (source: " + currentSourceNode + ", field: " + currentFieldAcc + ")";
    }
}

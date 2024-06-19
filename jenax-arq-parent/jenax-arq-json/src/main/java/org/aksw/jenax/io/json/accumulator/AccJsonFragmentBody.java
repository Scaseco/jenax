package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.path.json.PathJson.Step;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.io.rdf.json.RdfElement;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class AccJsonFragmentBody
    extends AccJsonBase
    implements AccJsonNode
{
    protected Map<Node, Integer> fieldIdToIndex = new HashMap<>();
    protected AccJsonEdge[] edgeAccs = new AccJsonEdge[0];

    protected int currentFieldIndex = -1;
    protected AccJsonEdge currentFieldAcc = null;


    /** Should not be used directly; use {@link AggJsonEdge} as the builder */
    public AccJsonFragmentBody() {
        this(new HashMap<>(), new AccJsonEdge[0]);
    }

    protected AccJsonFragmentBody(Map<Node, Integer> fieldIdToIndex, AccJsonEdge[] edgeAccs) {
        super();
        this.fieldIdToIndex = fieldIdToIndex;
        this.edgeAccs = edgeAccs;
    }

    /** Create a new instance and set it as the parent on all the property accumulators */
    public static AccJsonFragmentBody of(Map<Node, Integer> fieldIdToIndex, AccJsonEdge[] edgeAccs) {
        AccJsonFragmentBody result = new AccJsonFragmentBody(fieldIdToIndex, edgeAccs);
        for (AccJsonEdge acc : edgeAccs) {
            acc.setParent(result);
        }
        return result;
    }

    @Override
    public void begin(Node source, AccContext context, boolean skipOutput) throws IOException {
        super.begin(source, context, skipOutput);

        // Reset fields
        currentFieldIndex = -1;
        currentFieldAcc = null;

        if (!skipOutput) {
            if (context.isMaterialize()) {
                value = RdfElement.newObject(source); // new JsonObject();
            }

            if (context.isSerialize()) {
                // context.getJsonWriter().beginObject();
            }
        }
    }

    @Override
    public AccJson transition(Triple input, AccContext context) throws IOException {
        ensureBegun();

        Node inputFieldId = input.getPredicate();

        AccJson result = null;
        Integer inputFieldIndex = fieldIdToIndex.get(inputFieldId);
        if (inputFieldIndex != null) {
            int requestedFieldIndex = inputFieldIndex.intValue();

            // Detect if the requested field comes before the current field
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

            // Skip over the remaining fields - allow them to produce
            // values such as null or empty arrays
            for (int i = currentFieldIndex + 1; i < requestedFieldIndex; ++i) {
                AccJsonEdge edgeAcc = edgeAccs[i];
                edgeAcc.begin(null, context, skipOutput);
                edgeAcc.end(context);
            }

            currentFieldIndex = requestedFieldIndex;
            currentFieldAcc = edgeAccs[requestedFieldIndex];

            boolean isForward = currentFieldAcc.isForward();
            Node edgeInputSource = TripleUtils.getSource(input, isForward);

            if (!Objects.equals(edgeInputSource, currentSourceNode)) {
                throw new RuntimeException("should not happen - node at " + currentSourceNode + " but edge claims " + edgeInputSource);
            }

            currentFieldAcc.begin(edgeInputSource, context, skipOutput);
            result = currentFieldAcc.transition(input, context);
        }
        return result;
    }

    @Override
    public void end(AccContext context) throws IOException {
        ensureBegun();

        // Visit all remaining fields
        for (int i = currentFieldIndex + 1; i < edgeAccs.length; ++i) {
            AccJsonEdge edgeAcc = edgeAccs[i];
            // Edge.begin receives the target of an edge - but there is none so we pass null
            edgeAcc.begin(null, context, skipOutput); // TODO We need to tell fields that there is no value
            edgeAcc.end(context);
        }

        if (!skipOutput) {
            if (context.isMaterialize() && parent != null) {
                parent.acceptContribution(value, context);
            }

            if (context.isSerialize()) {
                // context.getJsonWriter().endObject();
            }
        }

        currentFieldIndex = -1;
        currentFieldAcc = null;
        super.end(context);
    }

    /** Internal method, use only for debugging/testing */
    public void addEdge(AccJsonEdge subAcc) {
        // TODO Lots of array copying!
        // We should add a builder for efficiet adds and derive the more efficient array version from it.
        Node fieldId = subAcc.getMatchFieldId();
        int fieldIndex = edgeAccs.length;
        fieldIdToIndex.put(fieldId, fieldIndex);
        edgeAccs = Arrays.copyOf(edgeAccs, fieldIndex + 1);
        edgeAccs[fieldIndex] = subAcc;
        subAcc.setParent(this);
    }

    @Override
    public PathJson getPath() {
        String stepName = currentFieldAcc == null ? "(no active field)" : Objects.toString(currentFieldAcc.getJsonKey());
        return (parent != null ? parent.getPath() : PathJson.newRelativePath()).resolve(Step.of(stepName));
    }

    @Override
    public String toString() {
        return "AccJsonNodeFragment (source: " + currentSourceNode + ", field: " + currentFieldAcc + ")";
    }

    @Override
    public void acceptContribution(RdfElement value, AccContext context) {
        throw new UnsupportedOperationException("This method should not be called on AccJsonNodeObject. It is AccJsonEdgeImpl that adds the contributions to an object.");
    }
}

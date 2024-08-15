package org.aksw.jenax.io.json.accumulator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.path.json.PathJson.Step;
import org.aksw.jenax.io.rdf.json.RdfElement;
import org.apache.jena.graph.Node;

public abstract class AccJsonObjectLikeBase
    extends AccJsonBase
    implements AccJsonObjectLike
{
    protected Map<Node, Integer> fieldIdToIndex = new HashMap<>();
    protected AccJsonEdge[] edgeAccs = new AccJsonEdge[0];

    protected int currentFieldIndex = -1;
    protected AccJsonEdge currentFieldAcc = null;

    protected AccJsonObjectLikeBase(Map<Node, Integer> fieldIdToIndex, AccJsonEdge[] edgeAccs) {
        super();
        this.fieldIdToIndex = fieldIdToIndex;
        this.edgeAccs = edgeAccs;
    }

    @Override
    public PathJson getPath() {
        String stepName = currentFieldAcc == null ? "(no active field)" : Objects.toString(currentFieldAcc.getJsonKey());
        return (parent != null ? parent.getPath() : PathJson.newRelativePath()).resolve(Step.of(stepName));
    }

//    @Override
//    public AccJson transition(Triple edge, AccContext cxt) throws IOException {
//        // TODO Auto-generated method stub
//        return null;
//    }

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
    public void acceptContribution(RdfElement value, AccContext context) {
        throw new UnsupportedOperationException("This method should not be called on AccJsonNodeObjectLike. The AccJsonEdge implementations add their contributions directly to their parent.");
    }
}

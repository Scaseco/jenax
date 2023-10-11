package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.commons.util.stream.SequentialGroupBySpec;
import org.aksw.commons.util.stream.StreamOperatorSequentialGroupBy;
import org.aksw.jenax.arq.json.RdfJsonUtils;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;


public class EdgeBasedAccumulator {
    public static void main(String[] args) throws Exception {
        AccJsonNodeObject rootAcc = new AccJsonNodeObject(null);

        AccJsonEdge labelEdgeAcc = new AccJsonEdgeImpl(rootAcc, "labelJsonKey", "urn:labelFieldId", true);
        AccJsonNodeLiteral labelValueAcc = new AccJsonNodeLiteral();

        labelEdgeAcc.setTargetAcc(labelValueAcc);

        AccJsonEdge commentEdgeAcc = new AccJsonEdgeImpl(rootAcc, "commentJsonKey", "urn:commentFieldId", true);
        AccJsonNodeLiteral commentValueAcc = new AccJsonNodeLiteral();

        commentEdgeAcc.setTargetAcc(commentValueAcc);

        rootAcc.addEdge(labelEdgeAcc);
        rootAcc.addEdge(commentEdgeAcc);

        List<Quad> data = Arrays.asList(
            create("urn:s1", "urn:s1", "urn:labelFieldId", "urn:l1"));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonWriter writer = gson.newJsonWriter(new OutputStreamWriter(System.out));

        AccContext accContext = new AccContext(gson, writer, true, true);
        accContext.serialize = true;
        accContext.materialize = true;


        AccNodeDriver driver = new AccNodeDriver(rootAcc);
        for (Quad quad : data) {
            driver.accumulate(quad, accContext);
        }
        System.out.println(driver.getValue());
    }

    public static Quad create(String g, String s, String p, String o) {
        return Quad.create(NodeFactory.createURI(g), NodeFactory.createURI(s), NodeFactory.createURI(p), NodeFactory.createURI(o));
    }
}

class AccContext {
    protected Gson gson;
    protected JsonWriter jsonWriter;

    public AccContext(Gson gson, JsonWriter jsonWriter, boolean materialize, boolean serialize) {
        super();
        this.gson = gson;
        this.jsonWriter = jsonWriter;
        this.materialize = materialize;
        this.serialize = serialize;
    }

    /** Whether to accumulate a JsonElement */
    protected boolean materialize;

    /** Whether to stream to the jsonWriter */
    protected boolean serialize;

    public boolean isMaterialize() {
        return materialize;
    }

    public boolean isSerialize() {
        return serialize;
    }

    public Gson getGson() {
        return gson;
    }

    public JsonWriter getJsonWriter() {
        return jsonWriter;
    }
}

interface AccJson {
    AccJson getParent();

    void begin(Node node, AccContext cxt) throws Exception;
    AccJson transition(Triple edge, AccContext cxt) throws Exception;

    /** End the accumulator's current node */
    void end(AccContext cxt) throws Exception;

    boolean hasBegun();

    /**
     * If cxt.isMaterialize is enabled then this method returns the json
     * data assembled for the current node.
     * It is only valid to call this method after end().
     */
    JsonElement getValue();

    void setParent(AccJson parent);
}

interface AccJsonNode
    extends AccJson
{

    // AccJson transition(Triple edge);
}

interface AccJsonEdge
    extends AccJson
{
    String getMatchFieldId();
    String getJsonKey();
    boolean isForward();

    void setTargetAcc(AccJson targetAcc);
}

class AccNodeDriver {
    protected AccJson currentAcc;
    protected Node currentSource;

    // protected JsonElement value;

    protected AccNodeDriver(AccJson rootAcc) {
        super();
        this.currentAcc = rootAcc;
    }

    public static AccNodeDriver of(AccJson rootAcc) {
        return new AccNodeDriver(rootAcc);
    }

    public void accumulate(Quad input, AccContext cxt) throws Exception {
        Node source = input.getGraph();
        Triple triple = input.asTriple();

        // If currentSource is set it implies we invoked beginNode()
        if (currentSource != null) {
            // If the input's source differs from the current one
            // then invoke end() on the accumulators up to the root
            if (!source.equals(currentSource)) {
                endCurrent(cxt);
            }
        }
        currentSource = source;
        // XXX Should we filter out the 'root quad' that announces the existence of a node?
        currentAcc.begin(currentSource, cxt);
        AccJson nextState;

        // Find a state that accepts the transition
        while (true) {
            nextState = currentAcc.transition(triple, cxt);
            if (nextState == null) {
                nextState.end(cxt);
                AccJson parentAcc = currentAcc.getParent();
                if (parentAcc != null) {
                    currentAcc = currentAcc.getParent();
                } else {
                    throw new RuntimeException("No acceptable transition for " + triple);
                }
            }
        }
    }

    public void begin(AccContext cxt) throws Exception {

    }

    public void end(AccContext cxt) throws Exception {
        endCurrent(cxt);
    }

    public JsonElement getValue() {
        return currentAcc.getValue();
    }

    protected void endCurrent(AccContext cxt) throws Exception {
        // Get the root of the currentAcc
        currentAcc = TreeUtils.findRoot(currentAcc, AccJson::getParent);
        // Ending the root recursively ends any inner nodes
        currentAcc.end(cxt);
//        while (true) {
//            currentAcc.end(node, cxt);
//            AccJson parent = currentAcc.getParent();
//            if (parent != null) {
//                currentAcc = parent;
//            } else {
//                break;
//            }
//        }
    }


    // This method needs to go to the aggregator because it needs to create an accumulator specifically
    // for the stream
    public Stream<Entry<Node, JsonElement>> asStream(Stream<Quad> quadStream) {
        Preconditions.checkArgument(!quadStream.isParallel(), "Json aggregation requires sequential stream");

        AccContext cxt = null; // enable materialize

        AccNodeDriver master = null; // new acc
        SequentialGroupBySpec<Quad, Node, AccNodeDriver> spec = SequentialGroupBySpec.create(
                Quad::getGraph,
                (accNum, collapseKey) -> { if (accNum != 0) { try {
                    master.end(cxt);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } } return master; },
                (acc, quad) -> {
                    try {
                        acc.accumulate(quad, cxt);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Stream<Entry<Node, JsonElement>> result = StreamOperatorSequentialGroupBy.create(spec)
            .transform(quadStream)
            .map(acc -> Map.entry(acc.getKey(), acc.getValue().getValue()));

        return result;
    }
}

abstract class AccJsonBase
    implements AccJson
{
    protected AccJson parent;
    protected JsonElement value = null;
    protected Node oldSourceNode; // Old value stored for debugging
    protected Node currentSourceNode;

    public AccJsonBase(AccJson parent) {
        super();
        this.parent = parent;
    }

    @Override
    public void setParent(AccJson parent) {
        this.parent = parent;
    }

    @Override
    public AccJson getParent() {
        return parent;
    }

    @Override
    public JsonElement getValue() {
        return value;
    }

    @Override
    public void begin(Node sourceNode, AccContext cxt) throws Exception {
        if (currentSourceNode != null) {
            throw new IllegalStateException("begin() has already been called() with " + currentSourceNode);
        }
        this.currentSourceNode = sourceNode;
    }

    @Override
    public void end(AccContext cxt) throws Exception {
        this.oldSourceNode = currentSourceNode;
        this.currentSourceNode = null;
    }

    @Override
    public boolean hasBegun() {
        return currentSourceNode != null;
    }
}

class AccJsonNodeLiteral
    extends AccJsonBase
    implements AccJsonNode
{

    public AccJsonNodeLiteral() {
        super(null);
    }

    @Override
    public void begin(Node node, AccContext context) throws Exception {
        // currentNode = node;
        // Node target = TripleUtils.getTarget(edge, isForward);

        // Always materialize literals
        value = RdfJsonUtils.toJson(Graph.emptyGraph, node, 0, 1, false);

        if (context.isSerialize()) {
            context.getGson().toJson(value, context.getJsonWriter());
        }
    }


    @Override
    public void end(AccContext context) throws IOException {
        // TODO Sanity check
    }

    @Override
    public AccJson transition(Triple edge, AccContext context) {
        // Literals reject all edges (indicated by null)
        return null;
    }
}

class AccJsonNodeObject
    extends AccJsonBase
    implements AccJsonNode
{
    public AccJsonNodeObject(AccJson parent) {
        super(parent);
    }

    protected Map<String, Integer> fieldIdToIndex = new HashMap<>();
    protected AccJsonEdge[] edgeAccs = new AccJsonEdge[0];

    protected int currentFieldIndex = -1;
    protected AccJsonEdge currentFieldAcc = null;

    // If a root node sees a new source then the start of a new result entity is assumed
    // If a non-root node sees a new source then it rejects it
    protected boolean isRootNode = false;

    @Override
    public void begin(Node source, AccContext context) throws Exception {
        super.begin(source, context);

        // Reset fields
        currentFieldIndex = -1;
        currentFieldAcc = null;

        if (context.isMaterialize()) {
            value = new JsonObject();
        }
        if (context.isSerialize()) {
            context.getJsonWriter().beginObject();
        }
    }

    @Override
    public void end(AccContext context) throws Exception {
        // end the current field (if any)
        if (currentFieldAcc != null) {
            currentFieldAcc.end(context);
        }

        // Visit all remaining fields
        for (int i = currentFieldIndex + 1; i < edgeAccs.length; ++i) {
            AccJsonEdge edgeAcc = edgeAccs[i];
            edgeAcc.begin(null, context); // TODO We need to tell fields that there is no value
            edgeAcc.end(context);

            String fieldName = edgeAcc.getJsonKey();
            JsonElement fieldValue = edgeAcc.getValue();
            if (context.isMaterialize()) {
                value.getAsJsonObject().add(fieldName, fieldValue);
            }
        }

        if (context.isSerialize()) {
            context.getJsonWriter().endObject();
        }

        currentFieldIndex = -1;
        currentFieldAcc = null;
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

            if (requestedFieldIndex != inputFieldIndex) {
                currentFieldAcc.end(context);

                for (int i = currentFieldIndex + 1; i < requestedFieldIndex; ++i) {
                    AccJsonEdge edgeAcc = edgeAccs[i];
                    edgeAcc.begin(null, context);
                    edgeAcc.end(context);
                }

                currentFieldIndex = requestedFieldIndex;
                currentFieldAcc = edgeAccs[requestedFieldIndex];

                boolean isForward = currentFieldAcc.isForward();
                Node target = TripleUtils.getTarget(input, isForward);

                currentFieldAcc.begin(target, context);
                result = currentFieldAcc.transition(input, context);
            }
        }
        return result;
    }

    public void addEdge(AccJsonEdge subAcc) {
        String fieldId = subAcc.getMatchFieldId();
        subAcc.setParent(this);
        int fieldIndex = edgeAccs.length;
        fieldIdToIndex.put(fieldId, fieldIndex);
        edgeAccs = Arrays.copyOf(edgeAccs, fieldIndex + 1);
    }
}


class AccJsonEdgeImpl
    extends AccJsonBase
    implements AccJsonEdge
{
    protected String matchFieldId; // AccJsonObject should index AccJsonEdge by this attribute
    protected boolean isForward;

    protected String jsonKey;

    protected AccJsonNode targetAcc;

    protected boolean isSingle;

    public AccJsonEdgeImpl(AccJson parent, String jsonKey, String matchFieldId, boolean isForward) {
        super(parent);
        this.matchFieldId = matchFieldId;
    }

    @Override
    public void setTargetAcc(AccJson targetAcc) {
        Preconditions.checkArgument(targetAcc.getParent() == null, "Parent already set");
        targetAcc.setParent(this);
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
    public void begin(Node node, AccContext context) throws Exception {
        super.begin(node, context);

        if (context.isMaterialize()) {
            value = new JsonArray();
        }

        if (context.isSerialize()) {
            JsonWriter jsonWriter = context.getJsonWriter();
            jsonWriter.name(jsonKey);
            if (!isSingle) {
                jsonWriter.beginArray();
            }
        }
    }

    @Override
    public void end(AccContext context) throws Exception {
        if (context.isSerialize()) {
            JsonWriter jsonWriter = context.getJsonWriter();
            if (!isSingle) {
                jsonWriter.endArray();
            }
        }
    }

    @Override
    public AccJson transition(Triple input, AccContext context) throws Exception {
        throw new RuntimeException("should not be called");
//        AccJson result = null;
//        String inputFieldId = input.getPredicate().getURI();
//        if (matchFieldId.equals(inputFieldId)) {
//
//            Node source = TripleUtils.getSource(input, isForward);
//
//            // The field id matches, but the source is a different one so its not 'our' edge
//            if (currentSource != null && !Objects.equals(source, currentSource)) {
//                targetAcc.end(context);
//            } else {
//                Node target = TripleUtils.getTarget(input, isForward);
//                targetAcc.begin(target, context);
//
//                if (targetAcc != null) {
//                    targetAcc.end();
//                }
//                result = targetAcc.transition(input, context);
//        } else {
//            end(writer);
//            // Indicate that the input was rejected
//            result = null;
//        }
//        return result;
    }
}




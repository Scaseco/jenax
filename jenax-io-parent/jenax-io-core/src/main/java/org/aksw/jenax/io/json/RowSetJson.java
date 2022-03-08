package org.aksw.jenax.io.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.atlas.data.BagFactory;
import org.apache.jena.atlas.data.DataBag;
import org.apache.jena.atlas.data.ThresholdPolicy;
import org.apache.jena.atlas.data.ThresholdPolicyFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.system.SerializationFactoryFinder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeFactoryExtra;

import com.google.common.collect.AbstractIterator;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;


/**
 * Streaming RowSet implementation for application/sparql-results+json
 * The {@link #getResultVars()} will return null as long as the header has not
 * been consumed from the underlying stream.
 *
 * Use {@link BufferedRowSet} to modify the behavior such that {@link #getResultVars()}
 * immediately consumes the underlying stream until the header is read,
 * thereby buffering any encountered bindings for replay.
 *
 * Use {@link #createBuffered(InputStream, Context)} to create a buffered row set
 * with appropriate configuration w.r.t. ARQ.inputGraphBNodeLabels and ThresholdPolicyFactory.
 *
 * @author Claus Stadler
 *
 */
public class RowSetJson
    extends AbstractIterator<Binding>
    implements RowSet
{

    public static RowSet createBuffered(InputStream in, Context context) {
        Context cxt = context == null ? ARQ.getContext() : context;

        boolean inputGraphBNodeLabels = cxt.isTrue(ARQ.inputGraphBNodeLabels);
        LabelToNode labelMap = inputGraphBNodeLabels
            ? SyntaxLabels.createLabelToNodeAsGiven()
            : SyntaxLabels.createLabelToNode();

        Supplier<DataBag<Binding>> bufferFactory = () -> {
            ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(cxt);
            DataBag<Binding> r = BagFactory.newDefaultBag(policy, SerializationFactoryFinder.bindingSerializationFactory());
            return r;
        };

        return createBuffered(in, labelMap, bufferFactory);
    }

    public static RowSet createBuffered(InputStream in, LabelToNode labelMap, Supplier<DataBag<Binding>> bufferFactory) {
        return new BufferedRowSet(createUnbuffered(in, labelMap), bufferFactory);
    }

    public static RowSet createUnbuffered(InputStream in, LabelToNode labelMap) {
        Gson gson = new Gson();
        JsonReader reader = gson.newJsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        RowSet result = new RowSetJson(gson, reader, LabelToNode.createUseLabelAsGiven());
        return result;
    }

    /** Parsing state; i.e. where we are in the json document */
    public enum State {
        INIT,
        ROOT,
        RESULTS,
        BINDINGS,
        DONE
    }


    protected Gson gson;
    protected JsonReader reader;

    protected List<Var> resultVars = null;
    protected long rowNumber;

    protected LabelToNode labelMap;

    // Hold the context for reference?
    // protected Context context;

    protected Function<JsonObject, Node> onUnknownRdfTermType = null;

    protected State state;


    public RowSetJson(Gson gson, JsonReader reader, LabelToNode labelMap) {
        this(gson, reader, labelMap, null, 0);
    }

    public RowSetJson(Gson gson, JsonReader reader, LabelToNode labelMap, List<Var> resultVars, long rowNumber) {
        super();
        this.gson = gson;
        this.reader = reader;
        this.labelMap = labelMap;

        this.resultVars = resultVars;
        this.rowNumber = rowNumber;

        this.state = State.INIT;
    }



    @Override
    public List<Var> getResultVars() {
        return resultVars;
    }

    @Override
    protected Binding computeNext() {
        try {
            return computeNextActual();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void onUnexpectedJsonElement() throws IOException {
        reader.skipValue();
    }


    protected Binding computeNextActual() throws IOException {
        Binding result;
        outer: while (true) {
            switch (state) {
            case INIT:
                reader.beginObject();
                state = State.ROOT;
                continue outer;

            case ROOT:
                while (reader.hasNext()) {
                    String topLevelName = reader.nextName();
                    switch (topLevelName) {
                    case "head":
                        resultVars = parseHead();
                        break;
                    case "results":
                        reader.beginObject();
                        state = State.RESULTS;
                        continue outer;
                    default:
                        onUnexpectedJsonElement();
                        break;
                    }
                }
                reader.endObject();
                state = State.DONE;
                continue outer;

            case RESULTS:
                while (reader.hasNext()) {
                    String elt = reader.nextName();
                    switch (elt) {
                    case "bindings":
                        reader.beginArray();
                        state = State.BINDINGS;
                        continue outer;
                    default:
                        onUnexpectedJsonElement();
                        break;
                    }
                }
                reader.endObject();
                state = State.ROOT;
                break;

            case BINDINGS:
                while (reader.hasNext()) {
                    result = parseBinding(gson, reader, labelMap, onUnknownRdfTermType);
                    ++rowNumber;
                    break outer;
                }
                reader.endArray();
                state = State.RESULTS;
                break;

            case DONE:
                result = endOfData();
                break outer;
            }
        }

        return result;
    }

    protected List<Var> parseHead() throws IOException {
        List<Var> result = null;

        reader.beginObject();
        String n = reader.nextName();
        switch (n) {
        case "vars":
            List<String> varNames = gson.fromJson(reader, new TypeToken<List<String>>() {}.getType());
            result = Var.varList(varNames);
            break;
        default:
            onUnexpectedJsonElement();
            break;
        }
        reader.endObject();

        return result;
    }

    @Override
    public long getRowNumber() {
        return rowNumber;
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new JenaException(e);
        }
    }


    public static Node parseOneTerm(JsonObject json, LabelToNode labelMap, Function<JsonObject, Node> onUnknownRdfTermType) {
        Node result;

        String type = json.get("type").getAsString();
        JsonElement valueJson = json.get("value");
        String valueStr;
        switch (type) {
        case "uri":
            valueStr = valueJson.getAsString();
            result = NodeFactory.createURI(valueStr);
            break;
        case "literal":
            valueStr = valueJson.getAsString();
            JsonElement langJson = json.get("xml:lang");
            JsonElement dtJson = json.get("datatype");
            result = NodeFactoryExtra.createLiteralNode(
                    valueStr,
                    langJson == null ? null : langJson.getAsString(),
                    dtJson == null ? null : dtJson.getAsString());
            break;
        case "bnode":
            valueStr = valueJson.getAsString();
            result = labelMap.get(null, valueStr);
            break;
        case "triple":
            JsonObject tripleJson = valueJson.getAsJsonObject();
            Node s = parseOneTerm(tripleJson.get("subject").getAsJsonObject(), labelMap, onUnknownRdfTermType);
            Node p = parseOneTerm(tripleJson.get("predicate").getAsJsonObject(), labelMap, onUnknownRdfTermType);
            Node o = parseOneTerm(tripleJson.get("object").getAsJsonObject(), labelMap, onUnknownRdfTermType);
            result = NodeFactory.createTripleNode(new Triple(s, p, o));
            break;
        default:
            if (onUnknownRdfTermType != null) {
                result = onUnknownRdfTermType.apply(json);
                Objects.requireNonNull(result, "Custom handler returned null for unknown rdf term type '" + type + "'");
            } else {
                throw new IllegalStateException("Unknown rdf term type: " + type);
            }
            break;
        }

        return result;
    }

    public static Binding parseBinding(
            Gson gson, JsonReader reader, LabelToNode labelMap,
            Function<JsonObject, Node> onUnknownRdfTermType) throws IOException {
        JsonObject obj = gson.fromJson(reader, JsonObject.class);

        BindingBuilder bb = BindingFactory.builder();

        for (Entry<String, JsonElement> e : obj.entrySet()) {
            Var v = Var.alloc(e.getKey());
            JsonObject nodeObj = e.getValue().getAsJsonObject();

            Node node = parseOneTerm(nodeObj, labelMap, onUnknownRdfTermType);
            bb.add(v, node);
        }

        return bb.build();
    }

}

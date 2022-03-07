package org.aksw.jenax.io.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.NodeFactoryExtra;

import com.google.common.base.Stopwatch;
import com.google.common.collect.AbstractIterator;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class RowSetJson
    extends AbstractIterator<Binding>
    implements RowSet
{

    public static void main(String[] args) throws MalformedURLException, IOException {
        RowSet rs = createBuffered(
                new URL("http://moin.aksw.org/sparql?query=SELECT%20*%20{%20?s%20?p%20?o%20}").openStream(),
                LabelToNode.createUseLabelAsGiven());

        System.out.println("ResultVars: " + rs.getResultVars());

        Stopwatch sw = Stopwatch.createStarted();
        for (int i = 0; i < 1000000 && rs.hasNext(); ++i) {
            rs.next();
            // System.out.println(rs.next());
        }
        System.out.println("Elapsed: " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001f + "s - final row: " + rs.getRowNumber());
        rs.close();
    }

    public static RowSet createBuffered(InputStream in, LabelToNode labelMap) {
        return new BufferedRowSet(create(in, labelMap));
    }

    public static RowSet create(InputStream in, LabelToNode labelMap) {
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
                    result = parseBinding(gson, reader, labelMap);
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

    public static Node parseOneTerm(JsonObject json, LabelToNode labelMap) {
        Node result;

        String value;
        String type = json.get("type").getAsString();
        switch (type) {
        case "uri":
            value = json.get("value").getAsString();
            result = NodeFactory.createURI(value);
            break;
        case "literal":
            value = json.get("value").getAsString();
            JsonElement langJson = json.get("lang");
            JsonElement dtJson = json.get("datatype");
            result = NodeFactoryExtra.createLiteralNode(
                    value,
                    langJson == null ? null : langJson.getAsString(),
                    dtJson == null ? null : dtJson.getAsString());
            break;
        case "bnode":
            value = json.get("value").getAsString();
            result = labelMap.get(null, value);
            break;
        case "triple":
            JsonObject tripleJson = json.get("value").getAsJsonObject();
            Node s = parseOneTerm(tripleJson.get("subject").getAsJsonObject(), labelMap);
            Node p = parseOneTerm(tripleJson.get("predicate").getAsJsonObject(), labelMap);
            Node o = parseOneTerm(tripleJson.get("object").getAsJsonObject(), labelMap);
            result = NodeFactory.createTripleNode(new Triple(s, p, o));
            break;
        default:
            result = null;
            break;
        }

        return result;
    }

    public static Binding parseBinding(Gson gson, JsonReader reader, LabelToNode labelMap) throws IOException {
        JsonObject obj = gson.fromJson(reader, JsonObject.class);

        BindingBuilder bb = BindingFactory.builder();

        for (Entry<String, JsonElement> e : obj.entrySet()) {
            Var v = Var.alloc(e.getKey());
            JsonObject nodeObj = e.getValue().getAsJsonObject();

            Node node = parseOneTerm(nodeObj, labelMap);
            bb.add(v, node);
        }

        return bb.build();
    }


    public List<Var> parseHead() throws IOException {
        List<Var> result = null;

        reader.beginObject();
        String n = reader.nextName();
        switch (n) {
        case "vars":
            result = gson.fromJson(reader, new TypeToken<List<String>>() {}.getType());
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
}

package org.aksw.jenax.graphql.sparql.v2.api.high;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.function.Function;

import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProviderApi;
import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProviderGson;
import org.aksw.jenax.graphql.sparql.v2.io.GraphQlIoBridge;
import org.aksw.jenax.graphql.sparql.v2.io.JsonWriterAdapter;
import org.aksw.jenax.graphql.sparql.v2.io.JsonWriterGson;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriterExt;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriterMapper;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriterMapperImpl;
import org.apache.jena.graph.Node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class GraphQlExecUtils {

    public static void write(OutputStream out, GraphQlExec<String> exec) throws IOException {
        write(out, exec, new GsonBuilder().serializeNulls().create());
    }

    public static void writePretty(OutputStream out, GraphQlExec<String> exec) throws IOException {
        write(out, exec, new GsonBuilder().serializeNulls().setPrettyPrinting().create());
    }

    public static void write(OutputStream out, GraphQlExec<String> exec, Gson gson) throws IOException {
        ObjectNotationWriterExt<String, JsonPrimitive, JsonWriterGson> destination = JsonWriterAdapter.of(gson.newJsonWriter(new OutputStreamWriter(out)), gson);

        // ObjectNotationWriterInMemory<T, String, V> destination = ObjectNotationWriterViaGon.of(jsonProvider);

        // GonProvider<String, V> gonProvider = jsonProvider;
        GonProviderApi<JsonElement, String, JsonPrimitive> gonProvider = GonProviderGson.of();
        // Function<P_Path0, String> keyMapper = GraphQlIoBridge::path0ToName;
        Function<String, String> keyMapper = x -> x; // identity
        Function<Node, JsonElement> valueMapper = GraphQlIoBridge::nodeToJsonElement;

        ObjectNotationWriterMapper<String, String, Node, JsonPrimitive> writer = new ObjectNotationWriterMapperImpl<>(destination, gonProvider, keyMapper, valueMapper);
        ObjectNotationWriter<String, Node> front = writer;

        // ObjectNotationWriterInMemory<T, P_Path0, Node> result = new ObjectNotationWriterInMemoryWrapper<T, P_Path0, Node>(front, destination);

        try {
            new GraphQlResultWriterImpl(gson).write(front, exec);
            out.write('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

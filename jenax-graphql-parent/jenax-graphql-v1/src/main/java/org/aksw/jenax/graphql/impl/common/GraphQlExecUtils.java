package org.aksw.jenax.graphql.impl.common;

import java.io.IOException;
import java.io.OutputStream;

import org.aksw.jenax.graphql.json.api.GraphQlDataProvider;
import org.aksw.jenax.graphql.json.api.GraphQlExec;
import org.aksw.jenax.graphql.json.api.GraphQlExecFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import graphql.language.Document;
import graphql.parser.Parser;

/** Utils to output the data from a GraphQlExec */
public class GraphQlExecUtils {

    public static JsonObject collectMetadata(GraphQlExec exec) {
        JsonObject metadata = new JsonObject();
        for (GraphQlDataProvider stream : exec.getDataProviders()) {
            JsonObject streamMetadata = stream.getMetadata();
            if (streamMetadata != null) {
                metadata.add(stream.getName(), streamMetadata);
            }
        }
        return metadata;
    }

    public static JsonObject collectExtensions(GraphQlExec exec) {
        JsonObject result = new JsonObject();
        JsonObject metadata = collectMetadata(exec);
        if (!metadata.keySet().isEmpty()) {
            result.add("metadata", metadata);
        }
        return result;
    }

    public static GraphQlExec execJson(GraphQlExecFactory gef, String jsonRequest) {
        return execJson(gef, new Gson(), jsonRequest);
    }

    public static GraphQlExec execJson(GraphQlExecFactory gef, Gson gson, String jsonRequest) {
        JsonObject jsonObject = gson.fromJson(jsonRequest, JsonObject.class);
        return execJson(gef, jsonObject);
    }

    public static GraphQlExec execJson(GraphQlExecFactory gef, JsonObject jsonObject) {
        Preconditions.checkArgument(jsonObject != null, "Expected a GraphQL document in JSON but got null");
        JsonElement queryElt = jsonObject.get("query");
        Preconditions.checkArgument(queryElt != null, "JSON object does not have a query field");
        Preconditions.checkArgument(queryElt.isJsonPrimitive(), "Value for 'query' is not a string");
        JsonPrimitive primitive = queryElt.getAsJsonPrimitive();
        Preconditions.checkArgument(primitive.isString(), "Value for 'query' must be a string");
        String queryStr = primitive.getAsString();
        GraphQlExec result = exec(gef, queryStr);
        return result;
    }

    /** Convenience method to execute a string */
    public static GraphQlExec exec(GraphQlExecFactory gef, String queryStr) {
        Parser parser = new Parser();
        Document document = parser.parseDocument(queryStr);
        GraphQlExec result = gef.create(document, null);
        return result;
    }

    /** Execute a graphql string and obtain the
     *  complete result document a guava json object */
    public static JsonObject materialize(GraphQlExecFactory gef, String queryStr) {
        GraphQlExec ge = exec(gef, queryStr);
        JsonObject result = GraphQlExecUtils.materialize(ge);
        return result;
    }

    /** Execute a graphql string and obtain the
     *  complete result document a guava json object */
    public static JsonObject materialize(GraphQlExec exec) {
        return new GraphQlExecToJsonObject().write(exec);
    }

    public static void write(OutputStream out, GraphQlExec exec) {
        write(out, exec, new GsonBuilder().serializeNulls().create());
    }

    public static void writePretty(OutputStream out, GraphQlExec exec) {
        write(out, exec, new GsonBuilder().serializeNulls().setPrettyPrinting().create());
    }

    public static void write(OutputStream out, GraphQlExec exec, Gson gson) {
        try {
            new GraphQlResultWriterImpl(gson).write(out, exec);
            out.write('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

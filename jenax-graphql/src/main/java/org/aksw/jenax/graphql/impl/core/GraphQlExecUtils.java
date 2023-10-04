package org.aksw.jenax.graphql.impl.core;

import java.io.IOException;
import java.io.OutputStream;

import org.aksw.jenax.graphql.GraphQlExec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/** Utils to output the data from a GraphQlExec */
public class GraphQlExecUtils {
    public static JsonObject materialize(GraphQlExec exec) {
        return new GraphQlExecToJsonObject().write(exec);
    }

    public static void write(OutputStream out, GraphQlExec exec) {
        write(out, exec, new Gson());
    }

    public static void writePretty(OutputStream out, GraphQlExec exec) {
        write(out, exec, new GsonBuilder().setPrettyPrinting().create());
    }

    public static void write(OutputStream out, GraphQlExec exec, Gson gson) {
        try {
            new GraphQlResponseWriterImpl(gson).write(out, exec);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

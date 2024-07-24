package org.aksw.jenax.graphql.impl.common;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.aksw.jenax.graphql.json.api.GraphQlDataProvider;
import org.aksw.jenax.graphql.json.api.GraphQlExec;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * Writes out the data streams of a GraphQlExec to the an OutputStream.
 */
public class GraphQlResultWriterImpl
{
    protected Gson gson;

    protected GraphQlResultWriterImpl(Gson gson) {
        super();
        this.gson = gson;
    }

    public Gson getGson() {
        return gson;
    }

    /** Wraps the output stream with a json writer. Always flushes the writer on completion. */
    public void write(OutputStream out, GraphQlExec exec) throws IOException {
        JsonWriter writer = gson.newJsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        write(writer, exec);
        writer.flush();
    }

    // @Override
    public void write(JsonWriter writer, GraphQlExec exec) throws IOException {
        writer.beginObject();
        // { data: { ...  dataStreams ... } }
        writer.name("data");
        writer.beginObject();

        for (GraphQlDataProvider dataProvider : exec.getDataProviders()) {
            String name = dataProvider.getName();
            writer.name(name);

            boolean isSingle = dataProvider.isSingle();
            if (!isSingle) {
                writer.beginArray();
            }

            dataProvider.write(writer, gson);

            if (!isSingle) {
                writer.endArray();
            }
        }

        writer.endObject(); // end data

        writer.name("errors");
        writer.beginArray();
        // TODO Write out encountered errors
        writer.endArray(); // end errors

        JsonObject metadata = GraphQlExecUtils.collectExtensions(exec);
        if (!metadata.keySet().isEmpty()) {
            writer.name("extensions");
            gson.toJson(metadata, writer);
        }

        writer.endObject(); // end response
    }
}

package org.aksw.jenax.graphql.impl.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jenax.graphql.GraphQlExec;
import org.aksw.jenax.graphql.GraphQlStream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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

        Set<String> dataStreamNames = exec.getDataStreamNames();
        for (String name : dataStreamNames) {
            writer.name(name);

            // TODO Handle the case of non-array responses
            writer.beginArray();
            GraphQlStream dataStream = exec.getDataStream(name);
            try (Stream<JsonElement> stream = dataStream.openStream()) {
                stream.forEach(item -> gson.toJson(item, writer));
            }
            writer.endArray();
        }

        writer.endObject(); // end data

        writer.name("errors");
        writer.beginArray();
        // TODO Write out encountered errors
        writer.endArray(); // end errors

        JsonObject metadata = GraphQlExecUtils.collectMetadata(exec);
        if (!metadata.keySet().isEmpty()) {
            writer.name("extensions");
            gson.toJson(metadata, writer);
        }


        writer.endObject(); // end response
    }
}

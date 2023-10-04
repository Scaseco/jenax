package org.aksw.jenax.graphql.impl.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jenax.graphql.GraphQlExec;
import org.aksw.jenax.graphql.GraphQlResponseWriter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

/**
 * Writes out the data streams of a GraphQlExec to the an OutputStream.
 */
public class GraphQlResponseWriterImpl
    implements GraphQlResponseWriter
{
    protected Gson gson;

    protected GraphQlResponseWriterImpl(Gson gson) {
        super();
        this.gson = gson;
    }

    public Gson getGson() {
        return gson;
    }

    @Override
    public void write(OutputStream out, GraphQlExec exec) throws IOException {
        JsonWriter writer = gson.newJsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        writer.beginObject();
        // { data: { ...  dataStreams ... } }
        writer.name("data");
        writer.beginObject();

        Set<String> dataStreamNames = exec.getDataStreamNames();
        for (String name : dataStreamNames) {
            writer.name(name);

            // TODO Handle the case of non-array responses
            writer.beginArray();
            try (Stream<JsonElement> stream = exec.getDataStream(name)) {
                stream.forEach(item -> gson.toJson(item, writer));
            }
            writer.endArray();
        }

        writer.endObject(); // end data

        writer.name("errors");
        writer.beginArray();
        // TODO Write out encountered errors
        writer.endArray(); // end errors

        writer.endObject(); // end response
    }
}

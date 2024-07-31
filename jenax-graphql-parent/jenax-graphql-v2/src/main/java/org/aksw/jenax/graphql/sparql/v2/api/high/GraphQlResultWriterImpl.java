package org.aksw.jenax.graphql.sparql.v2.api.high;

import java.io.IOException;

import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;

import com.google.gson.Gson;

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
//    public void write(OutputStream out, GraphQlExec exec) throws IOException {
//        JsonWriter writer = gson.newJsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
//        write(writer, exec);
//        writer.flush();
//    }
//
    public static P_Path0 hack(String name) {
        return new P_Link(NodeFactory.createLiteralString(name));
    }

    // @Override
    public void write(ObjectNotationWriter<String, Node> writer, GraphQlExec exec) throws IOException {
        writer.beginObject();
        // { data: { ...  dataStreams ... } }
        writer.name("data");

        if (!exec.isSingle()) {
            writer.beginArray();
        }

        while (exec.sendNextItemToWriter(writer)) {

        }

        if (!exec.isSingle()) {
            writer.endArray();
        }

//        for (GraphQlDataProvider dataProvider : exec.getDataProviders()) {
//            String name = dataProvider.getName();
//            writer.name(name);
//
//            boolean isSingle = dataProvider.isSingle();
//            if (!isSingle) {
//                writer.beginArray();
//            }
//
//            exec.
//            // dataProvider.write(writer, gson);
//
//            if (!isSingle) {
//                writer.endArray();
//            }
//        }

        // writer.endObject(); // end data

        writer.name("errors");
        writer.beginArray();
        // TODO Write out encountered errors
        writer.endArray(); // end errors

//        JsonObject metadata = GraphQlExecUtils.collectExtensions(exec);
//        if (!metadata.keySet().isEmpty()) {
//            writer.name("extensions");
//            gson.toJson(metadata, writer);
//        }

        writer.endObject(); // end response
        writer.flush();
    }
}

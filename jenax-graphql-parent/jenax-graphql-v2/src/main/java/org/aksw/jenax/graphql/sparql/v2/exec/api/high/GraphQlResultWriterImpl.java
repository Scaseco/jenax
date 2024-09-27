package org.aksw.jenax.graphql.sparql.v2.exec.api.high;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProviderJava;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriterUtils;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriterViaGon;
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

    public static P_Path0 stringToKey(String name) {
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
            // Loop until done
        }

        if (!exec.isSingle()) {
            writer.endArray();
        }

        writer.name("errors");
        writer.beginArray();
        // TODO Write out encountered errors
        writer.endArray(); // end errors

        // The code below collects extensions in-memory and writes out an { "extension": {} } section
        // if exec.writeExtensions returns a non-empty object.
        // XXX Too much boilerplate

        GonProviderJava<String, Node> gonProvider = GonProviderJava.newInstance();
        ObjectNotationWriterViaGon<Object, String, Node> memoryWriter = ObjectNotationWriterViaGon.of(gonProvider);
        memoryWriter.beginObject();
        exec.writeExtensions(memoryWriter, x -> x);
        memoryWriter.endObject();
        Object product = memoryWriter.getProduct();

        Iterator<Entry<String, Object>> it = gonProvider.listProperties(product);
        if (it.hasNext()) {
            writer.name("extensions");
            writer.beginObject();

            while (it.hasNext()) {
                Entry<String, Object> e = it.next();
                String k = e.getKey();
                Object v = e.getValue();
                writer.name(k);
                ObjectNotationWriterUtils.sendToWriter(writer, gonProvider, v);
            }

            writer.endObject();
        }

        writer.endObject(); // end response
        writer.flush();
    }
}

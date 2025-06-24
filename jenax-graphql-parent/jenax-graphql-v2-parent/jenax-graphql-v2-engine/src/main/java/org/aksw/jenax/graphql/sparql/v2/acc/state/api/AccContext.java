package org.aksw.jenax.graphql.sparql.v2.acc.state.api;

import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProvider;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;
import org.aksw.jenax.graphql.sparql.v2.io.RdfObjectNotationWriter;
import org.aksw.jenax.graphql.sparql.v2.io.RdfObjectNotationWriterViaJson;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

public class AccContext<K, V> {
    /** Whether to accumulate a JsonElement */
    protected boolean materialize;

    /** Whether to stream to the jsonWriter */
    protected boolean serialize;

    /** The writer for generalized object notation. May support streaming. */
    protected ObjectNotationWriter<K, V> writer;

    /** The provider for building in memory objects. */
    protected GonProvider<K, V> gonProvider;

    protected AccJsonErrorHandler errorHandler = null;

    public AccContext(ObjectNotationWriter<K, V> writer, boolean materialize, boolean serialize) {
        super();
        this.writer = writer;
        this.materialize = materialize;
        this.serialize = serialize;
    }

    /** Create a context that only materializes */
    public static AccContextRdf materializing() {
        return new AccContextRdf(null, true, false);
    }

    public static AccContextRdf serializing(Gson gson, JsonWriter jsonWriter) {
        RdfObjectNotationWriter writer = new RdfObjectNotationWriterViaJson(gson, jsonWriter);
        return serializing(writer);
    }

    public static AccContextRdf serializing(RdfObjectNotationWriter writer) {
        return new AccContextRdf(writer, false, true);
    }

    public static <K, V> AccContext<K, V> serializing(ObjectNotationWriter<K, V> writer) {
        return new AccContext<>(writer, false, true);
    }

    public void setWriter(ObjectNotationWriter<K, V> writer) {
        this.writer = writer;
    }

    public void setErrorHandler(AccJsonErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public AccJsonErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public boolean isMaterialize() {
        return materialize;
    }

    public boolean isSerialize() {
        return serialize;
    }

    public ObjectNotationWriter<K, V> getJsonWriter() {
        return writer;
    }

    public GonProvider<K, V> getGonProvider() {
        return gonProvider;
    }

    public void setGonProvider(GonProvider<K, V> gonProvider) {
        this.gonProvider = gonProvider;
    }

//public Gson getGson() {
//return gson;
//}

//public JsonWriter getJsonWriter() {
//return jsonWriter;
//}
}

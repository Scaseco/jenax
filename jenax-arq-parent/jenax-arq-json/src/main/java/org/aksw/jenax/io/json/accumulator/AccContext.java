package org.aksw.jenax.io.json.accumulator;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

public class AccContext {
//    protected Gson gson;
//    protected JsonWriter jsonWriter;

    protected StructuredWriterRdf writer;
    protected AccJsonErrorHandler errorHandler = null;

    public AccContext(StructuredWriterRdf writer, boolean materialize, boolean serialize) {
        super();
        this.writer = writer;
        this.materialize = materialize;
        this.serialize = serialize;
    }

    /** Create a context that only materializes */
    public static AccContext materializing() {
        return new AccContext(null,  true, false);
    }

    public static AccContext serializing(Gson gson, JsonWriter jsonWriter) {
        StructuredWriterRdf writer = new StructuredWriterRdfViaJson(gson, jsonWriter);
        return new AccContext(writer, false, true);
    }

    public void setErrorHandler(AccJsonErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public AccJsonErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /** Whether to accumulate a JsonElement */
    protected boolean materialize;

    /** Whether to stream to the jsonWriter */
    protected boolean serialize;

    public boolean isMaterialize() {
        return materialize;
    }

    public boolean isSerialize() {
        return serialize;
    }

    public StructuredWriterRdf getJsonWriter() {
        return writer;
    }

//  public Gson getGson() {
//  return gson;
//}

//public JsonWriter getJsonWriter() {
//  return jsonWriter;
//}

}
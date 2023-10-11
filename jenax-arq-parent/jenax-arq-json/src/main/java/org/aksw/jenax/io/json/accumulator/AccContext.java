package org.aksw.jenax.io.json.accumulator;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

class AccContext {
    protected Gson gson;
    protected JsonWriter jsonWriter;

    protected AccJsonErrorHandler errorHandler = null;

    public AccContext(Gson gson, JsonWriter jsonWriter, boolean materialize, boolean serialize) {
        super();
        this.gson = gson;
        this.jsonWriter = jsonWriter;
        this.materialize = materialize;
        this.serialize = serialize;
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

    public Gson getGson() {
        return gson;
    }

    public JsonWriter getJsonWriter() {
        return jsonWriter;
    }
}

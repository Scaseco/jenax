package org.aksw.jenax.io.json.accumulator;

import org.aksw.commons.path.json.PathJson;

public class AccJsonErrorEvent {
    /** Where the error occured */
    protected PathJson path;

    protected String message;

    public AccJsonErrorEvent(PathJson path, String message) {
        super();
        this.path = path;
        this.message = message;
    }

    public PathJson getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Json accumulation error at path " + path + ": " + message;
    }
}

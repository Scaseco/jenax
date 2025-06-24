package org.aksw.jenax.graphql.sparql.v2.acc.state.api;

public class AccJsonErrorEvent<K> {
    /** Where the error occured */
    protected PathGon<K> path;

    protected String message;

    public AccJsonErrorEvent(PathGon<K> path, String message) {
        super();
        this.path = path;
        this.message = message;
    }

    public PathGon<K> getPath() {
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

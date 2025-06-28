package org.aksw.shellgebra.unused.algebra.dag;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class StreamNode {
    private final String id;
    private final Map<String, Object> metadata;

    public StreamNode(String id) {
        this.id = id;
        this.metadata = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return id; // useful for printing
    }

    // Important: override equals and hashCode for correct graph behavior
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StreamNode)) return false;
        StreamNode other = (StreamNode) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Supplier<StreamNode> vertexSupplier() {
        int[] nextId = {0};
        return () -> new StreamNode("" + (nextId[0]++));
    }
}

package org.aksw.shellgebra.unused.algebra.dag;

import java.util.Objects;
import java.util.function.Supplier;

import org.aksw.shellgebra.algebra.common.OpSpec;

public class OpSpecNode {
    private final String id;
    private OpSpec opSpec;

    public static Supplier<OpSpecNode> vertexSupplier() {
        int[] nextId = {0};
        return () -> new OpSpecNode("" + (nextId[0]++));
    }

    public OpSpecNode(String id) {
        super();
        this.id = id;
    }
//    public OpSpecNode(String id, OpSpec opSpec) {
//        this.id = id;
//        this.opSpec = Objects.requireNonNull(opSpec);
//    }

    public String getId() {
        return id;
    }

    public OpSpecNode setOpSpec(OpSpec opSpec) {
        this.opSpec = opSpec;
        return this;
    }

    public OpSpec getOpSpec() {
        return opSpec;
    }

    @Override
    public String toString() {
        return "(" + id + ", " + opSpec + ")";
    }

    // Important: override equals and hashCode for correct graph behavior
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OpSpecNode)) return false;
        OpSpecNode other = (OpSpecNode) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

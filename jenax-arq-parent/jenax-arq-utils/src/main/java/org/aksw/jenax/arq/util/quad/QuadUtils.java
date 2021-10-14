package org.aksw.jenax.arq.util.quad;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.TupleSlot;

public class QuadUtils {

    private static final TupleSlot[] SLOTS = new TupleSlot[] {
            TupleSlot.GRAPH, TupleSlot.SUBJECT, TupleSlot.PREDICATE, TupleSlot.OBJECT };

    /** Access a triple's component by a zero-based index in order g, s, p, o.
     * Raises {@link IndexOutOfBoundsException} for any index outside of the range [0, 3]*/
    public static Node getNode(Quad quad, int idx) {
        switch (idx) {
        case 0: return quad.getGraph();
        case 1: return quad.getSubject();
        case 2: return quad.getPredicate();
        case 3: return quad.getObject();
        default: throw new IndexOutOfBoundsException("Cannot access index " + idx + " of a quad");
        }
    }

    public static TupleSlot idxToSlot(int idx) {
        return SLOTS[idx];
    }

    public static int slotToIdx(TupleSlot slot) {
        return slot.ordinal();
    }

    public static Node getNode(Quad quad, TupleSlot slot) {
        return getNode(quad, slotToIdx(slot));
    }

}

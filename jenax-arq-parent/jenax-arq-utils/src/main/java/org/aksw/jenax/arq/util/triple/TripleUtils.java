package org.aksw.jenax.arq.util.triple;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.mem.TupleSlot;

public class TripleUtils {

    private static final TupleSlot[] SLOTS = new TupleSlot[] {
            TupleSlot.SUBJECT, TupleSlot.PREDICATE, TupleSlot.OBJECT };


    /** Access a triple's component by a zero-based index in order s, p, o.
     * Raises {@link IndexOutOfBoundsException} for any index outside of the range [0, 2]*/
    public static Node getNode(Triple triple, int idx) {
        switch (idx) {
        case 0: return triple.getSubject();
        case 1: return triple.getPredicate();
        case 2: return triple.getObject();
        default: throw new IndexOutOfBoundsException("Cannot access index " + idx + " of a triple");
        }
    }

    public static TupleSlot idxToSlot(int idx) {
        return SLOTS[idx];
    }

    public static int slotToIdx(TupleSlot slot) {
        switch(slot) {
        case SUBJECT: return 0;
        case PREDICATE: return 1;
        case OBJECT: return 2;
        default: throw new IndexOutOfBoundsException("Cannot access slot " + slot + " of a triple");
        }
    }

    public static Node getNode(Triple triple, TupleSlot slot) {
        return getNode(triple, slotToIdx(slot));
    }

}

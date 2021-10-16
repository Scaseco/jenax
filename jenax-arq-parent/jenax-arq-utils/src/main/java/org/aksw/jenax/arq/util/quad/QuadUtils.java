package org.aksw.jenax.arq.util.quad;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.mem.TupleSlot;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.NodeTransform;

public class QuadUtils {

    /** Create o stream of a quad's four nodes */
    public static Stream<Node> streamNodes(Quad q) {
        return Stream.of(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
    }

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

    public static Quad applyNodeTransform(Quad quad,
            NodeTransform nodeTransform) {
        Node g = nodeTransform.apply(quad.getGraph());
        Node s = nodeTransform.apply(quad.getSubject());
        Node p = nodeTransform.apply(quad.getPredicate());
        Node o = nodeTransform.apply(quad.getObject());

        g = g != null ? g : quad.getGraph();
        s = s != null ? s : quad.getSubject();
        p = p != null ? p : quad.getPredicate();
        o = o != null ? o : quad.getObject();

        Quad result = new Quad(g, s, p, o);
        return result;
    }

    /**
     * Create a quad from an array
     *
     * @param nodes
     * @return
     */
    public static Quad create(Node[] nodes) {
        return new Quad(nodes[0], nodes[1], nodes[2], nodes[3]);
    }

    public static Node[] quadToArray(Quad quad) {
        return new Node[] { quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject() };
     }


    public static Node substitute(Node node, Binding binding) {
        Node result = node;

        if (node.isVariable()) {
            result = binding.get((Var) node);
            if (result == null) {
                throw new RuntimeException("Variable " + node + "not bound");
            }
        }

        return result;
    }


    public static Quad copySubstitute(Quad quad, Binding binding) {
        return new Quad(substitute(quad.getGraph(), binding),
                substitute(quad.getSubject(), binding),
                substitute(quad.getPredicate(), binding),
                substitute(quad.getObject(), binding));
    }


    public static Set<Var> getVarsMentioned(Quad quad) {
        return NodeUtils.getVarsMentioned(Arrays.asList(quadToArray(quad)));
    }


    public static Map<Node, Set<Quad>> partitionByGraph(Iterable<Quad> quads) {
        Map<Node, Set<Quad>> result = new HashMap<>();

        partitionByGraph(quads.iterator(), result, HashSet::new);

        return result;
    }

    public static <C extends Collection<Quad>, M extends Map<Node, C>> M partitionByGraph(
            Iterator<Quad> it,
            M result,
            Supplier<? extends C> supplier) {
        //Map<Node, Set<Quad>> result = new HashMap<Node, Set<Quad>>();
        while(it.hasNext()) {
            Quad quad = it.next();
            Node g = quad.getGraph();
            C qs = result.get(g);
            if (qs == null) {
                qs = supplier.get();
                result.put(g, qs);
            }
            qs.add(quad);
        }
        return result;
    }

    public static Map<Node, Set<Triple>> partitionByGraphTriples(Iterable<Quad> quads) {
        Map<Node, Set<Triple>> result = new HashMap<Node, Set<Triple>>();
        for (Quad quad : quads) {
            Node g = quad.getGraph();
            Set<Triple> ts = result.get(g);
            if (ts == null) {
                ts = new HashSet<Triple>();
                result.put(g, ts);
            }
            Triple t = quad.asTriple();
            ts.add(t);
        }
        return result;
    }


}

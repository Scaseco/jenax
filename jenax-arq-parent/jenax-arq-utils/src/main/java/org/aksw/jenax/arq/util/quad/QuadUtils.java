package org.aksw.jenax.arq.util.quad;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.arq.util.tuple.TupleAccessorQuad;
import org.aksw.jenax.arq.util.tuple.TupleUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.mem.TupleSlot;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

public class QuadUtils {

    public static final String ng = "g";
    public static final String ns = "s";
    public static final String np = "p";
    public static final String no = "o";

    public static final List<String> quadVarNames = Arrays.asList(ng, ns, np, no);


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

    public static List<Node> quadToList(Quad quad) {
        return Arrays.asList(quadToArray(quad));
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



    /** Apply a node transform to a given collection of quads
     *
     * @param <C> The result collection type
     * @param result The result collection; transform items will be added to it
     * @param quads The collection to transform
     * @param nodeTransform
     * @return The result collection
     */
    public static <C extends Collection<Quad>> C applyNodeTransform(C result, Collection<Quad> quads,
            NodeTransform nodeTransform) {
        for (Quad quad : quads) {
            Quad newQuad = applyNodeTransform(quad, nodeTransform);
            result.add(newQuad);
        }

        return result;
    }


    public static Binding quadToBinding(Quad quad) {
        Binding result = BindingBuilder.create().build();

        quadToBinding(quad, result);

        return result;
    }

    public static Binding quadToBinding(Quad quad, Binding parent) {
        BindingBuilder result = BindingBuilder.create(parent);
        result.add(Vars.g, quad.getGraph());
        TripleUtils.tripleToBinding(quad.asTriple(), parent);

        return result.build();
    }


    public static Binding quadToBinding(Quad pattern, Quad assignment) {
        return TupleUtils.tupleToBinding(TupleAccessorQuad.INSTANCE, pattern, assignment);
    }


    /** Partition the given quads by graph and create an appropriate element
     *  Makes use of ElementGroup, ElementNodeGraph and ElementTriplesBlock as necessary */
    public static Element toElement(Iterable<Quad> quads) {
        Map<Node, Set<Quad>> map = partitionByGraph(quads);
        Element result = toElement(map);
        return result;
    }


    public static Element toElement(Map<Node, Set<Quad>> graphToQuads) {
        ElementGroup es = new ElementGroup();
        for(Entry<Node, Set<Quad>> entry : graphToQuads.entrySet()) {
            ElementTriplesBlock e = new ElementTriplesBlock();
            for(Quad quad : entry.getValue()) {
                Triple triple = quad.asTriple();
                e.addTriple(triple);
            }

            Node graph = entry.getKey();

            Element f = graph == null || Quad.isDefaultGraph(graph)
                    ? e
                    : new ElementNamedGraph(graph, e);

            es.addElement(f);
        }

        Element result = es.getElements().size() == 1
                ? es.getElements().get(0)
                : es;

        return result;
    }

    /** Similar to NodeTransformLib.transformQuads but allows for use of an arbitrary collection type (other than list) */
    public static <C extends Collection<Quad>> C transformAll(C targetAcc, NodeTransform transform, Iterable<? extends Quad> source) {
        for (Quad quad : source) {
            Quad tgt = NodeTransformLib.transform(transform, quad);
            targetAcc.add(tgt);
        }
        return targetAcc;
    }
}

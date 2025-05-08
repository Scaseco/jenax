package org.aksw.jenax.arq.util.triple;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.commons.util.string.StringUtils;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.aksw.jenax.arq.util.io.NTripleUtils;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.tuple.TupleUtils;
import org.aksw.jenax.arq.util.tuple.adapter.TupleBridgeTriple;
import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.writer.NTriplesWriter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.mem.TupleSlot;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.sparql.util.NodeCmp;

public class TripleUtils {

    private static final TupleSlot[] SLOTS = new TupleSlot[] {
            TupleSlot.SUBJECT, TupleSlot.PREDICATE, TupleSlot.OBJECT };


    public static Stream<Node> streamNodes(Triple t) {
        return Stream.of(t.getSubject(), t.getPredicate(), t.getObject());
    }

    public static Iterator<Node> iterateNodes(Triple t) {
        return streamNodes(t).iterator();
    }

    public static boolean isValidAsStatement(Triple t) {
        return ModelUtils.isValidAsStatement(t.getSubject(), t.getPredicate(), t.getObject());
    }

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


    /**
     * Create a logical conjunction from two triple pattern.
     *
     * @param a
     * @param b
     * @return
     */
    public static Triple logicalAnd(Triple a, Triple b) {
        Node s = NodeUtils.logicalAnd(a.getMatchSubject(), b.getMatchSubject());
        Node p = NodeUtils.logicalAnd(a.getMatchPredicate(), b.getMatchPredicate());
        Node o = NodeUtils.logicalAnd(a.getMatchObject(), b.getMatchObject());

        Triple result = s == null || p == null || o == null
                ? null
                : Triple.createMatch(s, p, o);

        return result;
    }

//    public static Multimap<Node, Triple> indexBySubject(Iterable<Triple> triples) {
//        Multimap<Node, Triple> result = indexBySubject(triples.iterator());
//        return result;
//    }
//
//    public static Multimap<Node, Triple> indexBySubject(Iterator<Triple> it) {
//        Multimap<Node, Triple> result = HashMultimap.create();
//        while(it.hasNext()) {
//            Triple triple = it.next();
//            Node s = triple.getSubject();
//
//            result.put(s, triple);
//        }
//
//        return result;
//    }

    /**
     * If isForward is true then return the triple's subject otherwise its object.
     */
    public static Node getSource(Triple triple, boolean isForward) {
        return isForward ? triple.getSubject() : triple.getObject();
    }

    /**
     * If isForward is true then return the triple's object otherwise its subject.
     */
    public static Node getTarget(Triple triple, boolean isForward) {
        return isForward ? triple.getObject() : triple.getSubject();
    }

    /**
     * Create a matcher for triples having a certain predicate and a source node.
     * If 'isForward' is true then the subject acts as the source otherwise its the object.
     */
    public static Triple createMatch(Node source, P_Path0 predicate) {
        return createMatch(source, predicate.getNode(), predicate.isForward());
    }

    public static Triple createMatch(Triple triple, boolean isForward) {
        return isForward ? triple : swap(triple);
    }

    /**
     * Create a matcher for triples having a certain predicate and a source node.
     * If 'isForward' is true then the subject acts as the source otherwise its the object.
     */
    public static Triple createMatch(Node source, Node predicate, boolean isForward) {
        Triple result = isForward
                ? Triple.createMatch(source, predicate, Node.ANY)
                : Triple.createMatch(Node.ANY, predicate, source);

        return result;
    }

    public static Triple create(Node s, P_Path0 p, Node o) {
        Triple result = create(s, p.getNode(), o, p.isForward());
        return result;
    }

    public static Triple create(Node s, Node p, Node o, boolean isForward) {
        Triple result = isForward
            ? Triple.create(s, p, o)
            : Triple.create(o, p, s);

        return result;
    }

    public static Node[] toArray(Triple t) {
        Node[] result = new Node[] { t.getSubject(), t.getPredicate(), t.getObject() };
        return result;
    }

    public static Triple fromArray(Node[] nodes) {
        Node s = nodes[0];
        Node p = nodes[1];
        Node o = nodes[2];
        Triple result = Triple.create(s, p, o);
        return result;
    }

    public static Binding tripleToBinding(Triple triple) {
        Binding result = tripleToBinding(triple, Binding.noParent);
        return result;
    }

    public static Binding tripleToBinding(Triple triple, Binding parent) {
        BindingBuilder result = BindingBuilder.create(parent);
        result.add(Vars.s, triple.getSubject());
        result.add(Vars.p, triple.getPredicate());
        result.add(Vars.o, triple.getObject());
        return result.build();
    }

    public static Triple bindingToTriple(Binding b) {
        return bindingToTriple(b, Vars.s, Vars.p, Vars.o);
    }

    public static Triple bindingToTriple(Binding b, Var vs, Var vp, Var vo) {
        Triple result = null;
        Node s = b.get(vs);
        if (s != null) {
            Node p = b.get(vp);
            if (p != null) {
                Node o = b.get(vo);
                if (o != null) {
                    result = Triple.create(s, p, o);
                }
            }
        }
        return result;
    }

    public static Binding tripleToBinding(Triple pattern, Triple assignment) {
        return TupleUtils.tupleToBinding(TupleBridgeTriple.INSTANCE, pattern, assignment);
    }


//    public static String toNTripleString(Triple triple) {
//        String s = NodeUtils.toNTriplesString(triple.getSubject());
//        String p = NodeUtils.toNTriplesString(triple.getPredicate());
//        String o = NodeUtils.toNTriplesString(triple.getObject());
//
//        String result = s + " " + p + " " + o + " .";
//
//        return result;
//    }

    public static Triple swap(Triple t) {
        Triple result = Triple.create(t.getObject(), t.getPredicate(), t.getSubject());
        return result;
    }

    public static Set<Triple> swap(Iterable<Triple> triples) {
        Set<Triple> result = new HashSet<Triple>();

        for(Triple t : triples) {
            result.add(swap(t));
        }

        return result;
    }

    public static Triple listToTriple(List<Node> nodes) {
        return Triple.create(nodes.get(0), nodes.get(1), nodes.get(2));
    }

    public static List<Node> tripleToList(Triple triple)
    {
        List<Node> result = new ArrayList<Node>();
        result.add(triple.getSubject());
        result.add(triple.getPredicate());
        result.add(triple.getObject());

        return result;
    }

    /** Returns true if the triple survives a serialization/deserialization round trip */
    public static boolean isValid(Triple t) {
        boolean result;
        try {
            String str = NodeFmtLib.str(t) + " .";
            NTripleUtils.parseNTriplesString(str);
            result = true;
        } catch(Exception e) {
            result = false;
        }
        return result;
    }

    public static String md5sum(Triple triple) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NTriplesWriter.write(baos, Collections.singleton(triple).iterator());
        String raw = baos.toString();
        String result = StringUtils.md5Hash(raw);

        return result;
    }

    public static int compareRDFTerms(Triple o1, Triple o2) {
        return compare(o1, o2, NodeCmp::compareRDFTerms);
    }

//    public static int compareByValue(Triple o1, Triple o2) {
//    	return compare(o1, o2, NodeValueCmp::compareByValue);
//    }

    /**
     * Compare two triples by their nodes.
     *
     * @implNote
     *   This is jena4's TripleComparator.
     * */
    public static int compare(Triple o1, Triple o2, Comparator<Node> nc) {
        int toReturn = nc.compare(o1.getSubject(), o2.getSubject());
        if (toReturn == 0)
        {
            toReturn = nc.compare(o1.getPredicate(), o2.getPredicate());
            if (toReturn == 0)
            {
                toReturn = nc.compare(o1.getObject(), o2.getObject());
            }
        }
        return toReturn;
    }

    public static Triple copySubstitute(Triple quad, Binding binding) {
        return Triple.create(
            BindingUtils.substitute(quad.getSubject(), binding),
            BindingUtils.substitute(quad.getPredicate(), binding),
            BindingUtils.substitute(quad.getObject(), binding));
    }
}

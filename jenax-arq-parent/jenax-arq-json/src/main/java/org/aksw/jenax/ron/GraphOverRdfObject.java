package org.aksw.jenax.ron;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.node.NodeWrapper;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.Streams;

//class Node_RdfArrayItem
//    extends NodeWrapper<Entry<RdfArray, Integer>>
//{
//    @Override
//    protected Node getDelegate() {
//        return toNode(get().getKey().getInternalId(), get().getValue());
//    }
//
//}

class Node_RdfObject
    extends NodeWrapper<RdfObject>
{
    public Node_RdfObject(RdfObject obj) {
        super(obj);
    }

    @Override
    protected Node getDelegate() {
        Node result = get().getExternalId();
        if (result == null) {
            result = get().getInternalId();
        }
        return result;
    }

    public static Node of(RdfObject obj) {
        Objects.requireNonNull(obj);
        return new Node_RdfObject(obj);
    }
}

class Node_RdfLiteral
    extends NodeWrapper<RdfLiteral>
{
    public Node_RdfLiteral(RdfLiteral obj) {
        super(obj);
    }

    @Override
    protected Node getDelegate() {
        return get().getInternalId();
    }

    public static Node of(RdfLiteral obj) {
        Objects.requireNonNull(obj);
        return new Node_RdfLiteral(obj);
    }
}


/** Capture an offset in an array as a Node. */
class Node_RdfArrayAsRdfList
// 	extends NodeWrapper<>
{
    protected RdfArray arr;
    protected int index;
}


/**
 * A restricted graph implementation for traversal over object structures.
 * Either subject or object must be bound on lookups.
 *
 * TODO Implement support for viewing an RdfArray as a rdf:List.
 */
public class GraphOverRdfObject
    extends GraphBase
{
    /**
     * Stream the triples that the RdfObject holds. Recursively traverses the tree structure.
     * Does not handle cycles in the RdfElement structure.
     */
    public static Stream<Triple> streamTriples(RdfObject obj) {
        return obj.getMembers().entrySet().stream().flatMap(e -> {
            Node s = obj.getExternalId();
            if (s == null) {
                s = obj.getInternalId();
            }
            P_Path0 k = e.getKey();
            RdfElement elt = e.getValue();
            return streamTriples(s, k.getNode(), elt, k.isForward());
        });
    }

    public static Stream<Triple> streamTriples(Node s, Node p, RdfElement elt, boolean isForward) {
        Stream<Triple> result;
        if (elt.isNull()) {
            result = Stream.of();
        } else if (elt.isLiteral()) {
            Node o = elt.getAsLiteral().getInternalId();
            Triple t = TripleUtils.create(s, p, o, isForward);
            result = Stream.of(t);
        } else if (elt.isObject()) {
            RdfObject obj = elt.getAsObject();
            Node o = obj.getExternalId();
            if (o == null) {
                o = obj.getInternalId();
            }
            Triple t = TripleUtils.create(s, p, o, isForward);
            result = Stream.concat(Stream.of(t), streamTriples(obj));
        } else if (elt.isArray()) {
            result = Streams.stream(elt.getAsArray()).flatMap(item -> streamTriples(s, p, item, isForward));
        } else {
            throw new RuntimeException("should not come here");
        }

        return result;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
        Node ms = triplePattern.getMatchSubject();
        Node mp = triplePattern.getMatchPredicate();
        Node mo = triplePattern.getMatchObject();

        Iter<Triple> it = null;
        if (ms instanceof Node_RdfObject so) {
            it = match(so, mp, mo, true);
        }

        if (mo instanceof Node_RdfObject oo) {
            Iter<Triple> it2 = match(oo, mp, ms, false);
            it = it == null ? it2 : Iter.concat(it, it2);
        }

        return it == null ? NiceIterator.emptyIterator() : WrappedIterator.create(it);
    }

//    public Iter<Triple> match(Node_RdfArray src, Node p, Node o, boolean isForward) {
//    	//
//    }

    public Iter<Triple> match(Node_RdfObject src, Node p, Node o, boolean isForward) {

        Iter<Entry<P_Path0, RdfElement>> itMembers;
        if (NodeUtils.isNullOrAny(p)) {
            itMembers = Iter.iter(src.get().getMembers().entrySet()).filter(e -> e.getKey().isForward() == isForward);
        } else {
            P_Path0 pp = isForward ? new P_Link(p) : new P_ReverseLink(p);
            itMembers = Iter.ofNullable(src.get().getMembers().get(pp)).map(x -> Map.entry(pp, x));;
        }

        return itMembers.flatMap(e -> {
            P_Path0 pp = e.getKey();

            RdfElement elt = e.getValue();
            Iter<RdfElement> values = elt.isArray()
                    ? Iter.iter(elt.getAsArray().iterator())
                    : Iter.of(elt);

            Iter<RdfElement> filteredValues = NodeUtils.isNullOrAny(o)
                ? values
                : values.filter(v -> matches(v, o));

            return filteredValues
                    .map(x -> TripleUtils.create(src, pp.getNode(), wrap(x), isForward));
        });
    }

    public static Node wrap(RdfElement elt) {
        if (elt.isArray()) {
            throw new RuntimeException("should not happen");
        } else if (elt.isLiteral()) {
            return elt.getAsLiteral().getInternalId();
        } else if (elt.isObject()) {
            return Node_RdfObject.of(elt.getAsObject());
        } else {
            throw new RuntimeException("should not happed");
        }
    }

    public static boolean matches(RdfElement elt, Node node) {
        if (node instanceof Node_RdfObject no) {
             if (elt == no.get()) {
                 return true;
             }
        }

        if (elt.isObject()) {
            RdfObject obj = elt.getAsObject();

            Node externalId = obj.getExternalId();
            if (externalId != null) {
                return externalId.equals(node);
            }

            Node internalId = obj.getInternalId();
            return internalId.equals(node);
        } else if (elt.isLiteral()) {
            return elt.getAsLiteral().getInternalId().equals(node);
        }

        return false;
    }

    /** Convert an Array to an RdfElement that presents an rdf:List structure. */
    public static RdfElement arrayToRdfList(RdfArray arr) {
        Iterator<RdfElement> it = arr.iterator();

        RdfObject root = null;
        RdfObject previous = null;
        while (it.hasNext()) {
            RdfElement item = it.next();
            if (!item.isNull()) {
                // Set rest of the previous item to this

                RdfObject current = new RdfObjectImpl();
                current.addForward(RDF.first, item);

                if (previous == null) {
                    root = current;
                } else {
                    previous.addForward(RDF.rest, current);
                }

                previous = current;
            }
        }

        if (previous != null) {
            previous.addForward(RDF.rest, new RdfLiteralImpl(RDF.nil));
        }

        RdfElement result = root == null
                ? new RdfLiteralImpl(RDF.nil)
                : root;
        return result;
    }

//    interface Test {
//        @Iri("http://xmlns.com/foaf/0.1/knows")
//        @Reverse
//        Node getKnownFrom();
//    }

    public static void main(String[] args) {
        RdfObject s1 = new RdfObjectImpl();
        s1.addForward(RDF.type, new RdfLiteralImpl(OWL.Class));
        s1.addForward(RDFS.label, new RdfLiteralImpl(NodeFactory.createURI("s1")));

        RdfArray arr1 = new RdfArrayImpl();

        s1.addBackward(FOAF.knows, arr1);


        RdfObject s2 = new RdfObjectImpl();
        s2.addForward(RDF.type, new RdfLiteralImpl(RDFS.Class));
        s2.addForward(RDFS.label, new RdfLiteralImpl(NodeFactory.createURI("s2")));

        RdfArray arr2 = new RdfArrayImpl();

        s1.addForward(RDFS.seeAlso, s2);

        // arr2.getOrCreateContext().add("@container", "@list");


        // s2.getOrCreateContext().addForward("@container", "@list");
        // So how to declare the mapping of the RdfArray to an RDFlist?
        // Or do we need separate RDF datatypes from which we can derive JSON arrays?
        // We could have an RdfList datatype which internally stores a mapping (internalItemNode, itemValue)
        // List<Entry2<Node, Node>>



        arr2.add(new RdfLiteralImpl(NodeFactory.createURI("incoming1")));
        arr2.add(new RdfLiteralImpl(NodeFactory.createURI("incoming2")));
        arr2.add(new RdfLiteralImpl(NodeFactory.createURI("incoming3")));


        s2.addForward(FOAF.knows, arrayToRdfList(arr2));

//        Node node = Node_RdfObject.of(s1);
//
//        Graph g = new GraphOverRdfObject();
//        Model model = ModelFactory.createModelForGraph(g);
//        Resource r = model.asRDFNode(node).asResource();
        Resource r = s2.as(Resource.class);

        r.listProperties().forEach(stmt -> System.out.println(stmt));

        Model model = r.getModel();

        // model.listResourcesWithProperty(null, r).forEach(xxx -> System.out.println("Got: " + xxx));

        Model closure = ResourceUtils.reachableClosure(r);
        RDFDataMgr.write(System.out, closure, RDFFormat.NTRIPLES);

        // r.listProperties().forEach(stmt -> System.out.println("Stmt: " + stmt));

    }
}


class GraphOverRdfObjectOld
    extends GraphBase
{
    protected RdfObject rdfObject;

    // The nodeSchema could be used to provide information about what rdf object types to allocate for new properties
    // protected NodeSchema

    protected boolean matchesObjectId(Node node) {
        boolean result = node.equals(rdfObject.getExternalId()) || node.equals(rdfObject.getInternalId());
        return result;
    }

    @Override
    public void add(Node s, Node p, Node o) throws AddDeniedException {
        // The special case (:x :p :x) will only add to the reverse direction if it already exists
        // Otherwise it will always add in forward direction

        // boolean isAmbiguous = s.equals(o);
        boolean isBackward = matchesObjectId(o);

        boolean handled = false;

        if (isBackward) {
            P_Path0 bwdP = new P_ReverseLink(p);
            RdfElement elt = rdfObject.getMembers().get(bwdP);

            if (elt != null) {
                if (elt.isArray()) {
                    RdfArray arr = elt.getAsArray();
                    RdfElement matchItem = null;
                    for (int i = 0; i < arr.size(); ++i) {
                        RdfElement item = arr.get(i);
                        if (item.isObject()) {
                            RdfObject obj = item.getAsObject();
                            boolean isMatch = matchesObjectId(s);

                            if (isMatch) {
                                matchItem = item;
                            }
                        }
                    }

                    if (matchItem == null) {
                        arr.add(new RdfObjectImpl(s));
                        handled = true;
                    }
                }
            } else {
                RdfArray arr = new RdfArrayImpl();
                arr.add(new RdfObjectImpl(s));
                rdfObject.addBackward(p, arr);
                handled = true;
            }
        }

        if (!handled) {
            boolean isForward = s.equals(rdfObject.getExternalId()) || s.equals(rdfObject.getInternalId());

            if (isForward) {
                P_Path0 fwdP = new P_Link(p);
                RdfElement elt = rdfObject.getMembers().get(fwdP);

                if (elt != null) {
                    if (elt.isArray()) {
                        RdfArray arr = elt.getAsArray();
                        RdfElement matchItem = null;
                        for (int i = 0; i < arr.size(); ++i) {
                            RdfElement item = arr.get(i);
                            if (item.isObject()) {
                                RdfObject obj = item.getAsObject();
                                boolean isMatch = o.equals(rdfObject.getExternalId()) || o.equals(rdfObject.getInternalId());

                                if (isMatch) {
                                    matchItem = item;
                                }
                            }
                        }

                        if (matchItem == null) {
                            arr.add(new RdfObjectImpl(o));
                            handled = true;
                        }
                    }
                } else {
                    RdfArray arr = new RdfArrayImpl();
                    arr.add(new RdfObjectImpl(s));
                    rdfObject.addForward(p, arr);
                    handled = true;
                }
            }

        }
    }

    // Create first / rest / nil triples for an array.
    // Use the array's internal id as a prefix for all intermediate nodes
    public static ExtendedIterator<Triple> toTriples(RdfArray arr, Triple pattern) {
        Node internalId = arr.getInternalId();
        String prefix = internalId.getBlankNodeLabel();

        Node s = pattern.getMatchSubject();
        Node mp = pattern.getMatchPredicate();

        List<Node> schemaPs = Arrays.asList(RDF.first.asNode(), RDF.rest.asNode());

        List<Node> emitPs;
        if (NodeUtils.isNullOrAny(mp)) {
            emitPs = schemaPs;
        } else {
            emitPs = schemaPs.stream().filter(item -> mp.matches(item)).toList();
        }

        int start = 0;
        int end = 0;

        for (int i = start; i < end; ++i) {
            Node emitS = toItemNode(internalId, i);
            RdfElement itemElt = arr.get(i);

            for (Node emitP : emitPs) {
                Node emitO = null;
                if (emitP.equals(RDF.first.asNode())) {
                    if (!itemElt.isNull()) {
                        emitO = null; // itemElt.getNode();
                    }
                } else if (emitP.equals(RDF.rest.asNode())) {
                    emitO = i == arr.size() ? RDF.nil.asNode() : toItemNode(internalId, i + 1);
                }

                if (emitO != null) {
                    Triple t = Triple.create(s, emitP, emitO);
                }
            }

        }

        // itemNode_1
        //   rdf:first userItem ;
        //   rdf:rest  itemNode_2 / rdf:nil .
        if (s != null && s.isBlank()) {
            String lookupId = s.getBlankNodeLabel();

            if (lookupId.startsWith(prefix)) {
                // _:blankNodeLabel_1
                String itemIndexStr = lookupId.substring(prefix.length() + 1);

                long itemIndex = Long.parseLong(itemIndexStr);

            }
        }

        return null;
    }

    public static int toIndex(Node base, Node itemNode) {
        String prefixStr = base.getBlankNodeLabel();

        String itemNodeLabel = itemNode.getBlankNodeLabel();
        int result;
        if (itemNodeLabel.startsWith(prefixStr)) {
            // _:blankNodeLabel_1
            String itemIndexStr = itemNodeLabel.substring(prefixStr.length() + 1);
            result = Integer.parseInt(itemIndexStr);

        } else {
            throw new IllegalArgumentException();
        }
        return result;
    }

    public static Node toItemNode(Node base, int index) {
        Node result;
        if (index == 0) {
            result = base;
        } else {
            String bnodeLabel = base.getBlankNodeLabel();
            result = NodeFactory.createBlankNode(bnodeLabel + "_" + index);
        }
        return result;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple pattern) {
        Node s = pattern.getMatchSubject();
        Node p = pattern.getMatchPredicate();
        Node o = pattern.getMatchObject();

        if (p != null) {

            P_Path0 fwdP = new P_Link(p);

            if (matchesObjectId(s)) {

                {
                    RdfElement elt = rdfObject.getMembers().get(fwdP);

                    // If o is concrete)

                    if (elt != null && !elt.isNull()) {
                        if (elt.isArray()) {
                            elt.getAsArray();
                        }


                    }

                }

                {
                    P_Path0 bwdP = new P_ReverseLink(p);
                    rdfObject.getMembers().get(bwdP);

                }
            }
        } else {

            // Iterate all p's
            rdfObject.getMembers().entrySet().stream().flatMap(entry -> {
                P_Path0 k = entry.getKey();

                boolean isForward = k.isForward();
                Node pred = k.getNode();

                return null;
            });

        }


        // TODO Auto-generated method stub
        return null;
    }

}

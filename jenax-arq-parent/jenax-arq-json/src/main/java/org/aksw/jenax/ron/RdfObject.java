package org.aksw.jenax.ron;

import java.util.Map;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;

public interface RdfObject
    extends RdfElementResource
{
    /** Get the members of this object. */
    // XXX We may want to add support for dedicated forward / backward views
    Map<P_Path0, RdfElement> getMembers();

    // RdfObject add(P_Path0, RdfElement value);

    RdfObject addForward(Node property, RdfElement value);
    RdfObject addForward(RDFNode property, RdfElement value);

    RdfObject addBackward(Node property, RdfElement value);
    RdfObject addBackward(RDFNode property, RdfElement value);


//    default RdfObject addForward(Node property, Node value) {
//        addForward(property, new RdfLiteralImpl(value));
//        return this;
//    }

    default RdfObject addForward(String key, String value) {
        Node k = NodeFactory.createLiteralString(key);
        RdfElement v = new RdfLiteralImpl(NodeFactory.createLiteralString(value));
        addForward(k, v);
        return this;
    }

    default RdfObject add(Node key, boolean isForward, RdfElement value) {
        P_Path0 p = isForward ? new P_Link(key) : new P_ReverseLink(key);
        add(p, value);
        return this;
    }

    default RdfObject addStr(String name, RdfElement value) {
        add(new P_Link(NodeFactory.createLiteralString(name)), value);
        return this;
    }

    default RdfObject add(P_Path0 key, RdfElement value) {
        getMembers().put(key, value);
        return this;
    }

    default RdfElement get(String name) {
        return get(NodeFactory.createLiteralString(name));
    }

    default RdfObject getObject(String name) {
        RdfElement elt = get(name);
        RdfObject result = elt == null ? null : elt.getAsObject();
        return result;
    }

    default RdfElement getIri(String iriStr) {
        return get(NodeFactory.createURI(iriStr));
    }

    default RdfElement get(Node node) {
        return getMembers().get(new P_Link(node));
    }

    default RdfElement get(RDFNode rdfNode) {
        return get(rdfNode.asNode());
    }

    default RdfElement get(P_Path0 key) {
        return getMembers().get(key);
    }

    default RdfObject remove(P_Path0 key) {
        Map<P_Path0, RdfElement> map = getMembers();
        RdfElement elt = map.get(key);
        if (elt != null) {
            ((RdfElementNodeBase)elt).setParent(null);
            map.remove(key);
        }
        return this;
    }

//
//    default RdfObject getOrCreateContext() {
//        String name = "@context";
//        RdfObject result;
//        RdfElement elt = get(name);
//        if (elt == null) {
//            result = new RdfObjectImpl();
//            P_Path0 key = new P_Link(NodeFactory.createLiteralString(name));
//            add(key, result);
//        } else {
//            result = elt.getAsObject();
//        }
//        return result;
//    }

    default <T extends RDFNode> T as(Class<T> cls) {
        Node node = Node_RdfObject.of(this);
        Graph g = new GraphOverRdfObject();
        Model model = ModelFactory.createModelForGraph(g);
        RDFNode rdfNode = model.asRDFNode(node);
        T result = rdfNode.as(cls);
        return result;
    }

//    static RdfObject extractRdfObject(Model model) {
//    	Graph g = model.getGraph();
//    	if (g instanceof GraphOverRdfObject)
//    }

    /** Return a mutable sub view of all forward keys that are strings and that can thus be viewed as json objects. */
    // getJsonSubView()
}

package org.aksw.jenax.io.rdf.json;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;

/**
 * An RDF counterpart to a JSON object.
 * Member properties can have a forward or backward direction which
 * allows for deriving an RDF graph.
 */
public class RdfObjectImpl
    extends RdfElementResourceBase
    implements RdfObject
{
    protected Map<P_Path0, RdfElement> members;

    public RdfObjectImpl() {
        this(null, new LinkedHashMap<>());
    }

    public RdfObjectImpl(Node node) {
        this(node, new LinkedHashMap<>());
    }

    protected RdfObjectImpl(Node node, HashMap<P_Path0, RdfElement> members) {
        super(node);
        this.members = members;
    }

    /** Return an RdfObject for the given node. The argument must not be null. */
//    public static RdfObject of(Node node) {
//        return new RdfObject(node);
//    }

    public RdfObject addForward(Node property, RdfElement value) {
        members.put(new P_Link(property), value);
        return this;
    }

    public RdfObject addForward(RDFNode property, RdfElement value) {
        addForward(property.asNode(), value);
        return this;
    }

    public RdfObject addBackward(Node property, RdfElement value) {
        members.put(new P_ReverseLink(property), value);
        return this;
    }

    public RdfObject addBackward(RDFNode property, RdfElement value) {
        addBackward(property.asNode(), value);
        return this;
    }


    public RdfObject add(P_Path0 path, RdfElement value) {
        members.put(path, value);
        return this;
    }

    public Map<P_Path0, RdfElement> getMembers() {
        return members;
    }

//    public RdfObject add(Node property, Node node) {
//        add(property, RdfElement.(node));
//        return this;
//    }

    @Override
    public <T> T accept(RdfElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


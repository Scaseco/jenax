package org.aksw.jenax.io.rdf.json;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.graph.Node;

public class RdfObject
    extends RdfElementNodeBase
{
    protected LinkedHashMap<Node, RdfElement> members;

    public RdfObject(Node node) {
        this(node, new LinkedHashMap<>());
    }

    protected RdfObject(Node node, LinkedHashMap<Node, RdfElement> members) {
        super(node);
        this.members = members;
    }

    /** Return an RdfObject for the given node. The argument must not be null. */
//    public static RdfObject of(Node node) {
//        return new RdfObject(node);
//    }

    public RdfObject add(Node property, RdfElement value) {
        members.put(property, value);
        return this;
    }

    public Map<Node, RdfElement> getMembers() {
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


package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueVisitor;

import com.google.gson.JsonElement;

public class NodeValueJson extends NodeValue {
    protected JsonElement jsonElement;

    public NodeValueJson(JsonElement jsonElement) {
        super();
        this.jsonElement = jsonElement;
    }

    public NodeValueJson(JsonElement jsonElement, Node n) {
        super(n);
        this.jsonElement = jsonElement;
    }

    public JsonElement getJsonElement() {
        return jsonElement;
    }

    @Override
    protected Node makeNode() {
        return NodeFactory.createLiteralByValue(jsonElement, RDFDatatypeJson.get());
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public String toString()
    {
        // Preserve lexical form
        String result = getNode() != null
                ? super.asString()
                : RDFDatatypeJson.get().unparse(jsonElement);
        return result;
    }

    @Override
    public void visit(NodeValueVisitor visitor) {
        // Unsupported operation?
    }
}

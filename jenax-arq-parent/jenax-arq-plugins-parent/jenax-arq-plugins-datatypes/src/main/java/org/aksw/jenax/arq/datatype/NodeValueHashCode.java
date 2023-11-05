package org.aksw.jenax.arq.datatype;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueVisitor;

import com.google.common.hash.HashCode;

public class NodeValueHashCode extends NodeValue {
    protected HashCode hashCode;

    public NodeValueHashCode(HashCode hashCode) {
        super();
        this.hashCode = hashCode;
    }

    public NodeValueHashCode(HashCode hashCode, Node n) {
        super(n);
        this.hashCode = hashCode;
    }

    public HashCode getHashCode() {
        return hashCode;
    }

    @Override
    protected Node makeNode() {
        return NodeFactory.createLiteralByValue(hashCode, RDFDatatypeHashCode.get());
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
                : RDFDatatypeHashCode.get().unparse(hashCode);
        return result;
    }

    @Override
    public void visit(NodeValueVisitor visitor) {
        // Unsupported operation?
    }
}

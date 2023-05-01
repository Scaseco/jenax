package org.aksw.jenax.arq.datatype.lambda;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueVisitor;

public class NodeValueLambda extends NodeValue {
    protected Lambda lambda;

    public NodeValueLambda(Lambda lambda) {
        super();
        this.lambda = lambda;
    }

    public NodeValueLambda(Lambda lambda, Node n) {
        super(n);
        this.lambda = lambda;
    }

    public Lambda getLambda() {
        return lambda;
    }

    @Override
    protected Node makeNode() {
        return NodeFactory.createLiteralByValue(lambda, RDFDatatypeLambda.get());
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public String toString() {
        // Preserve lexical form
        String result = getNode() != null
                ? super.asString()
                : RDFDatatypeLambda.get().unparse(lambda);
        return result;
    }

    @Override
    public void visit(NodeValueVisitor visitor) { }
}

package org.aksw.jena_sparql_api.sparql.ext.nodemap;

import org.aksw.jenax.arq.datatype.RDFDatatypeNodeMap;
import org.aksw.jenax.arq.util.node.NodeMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueVisitor;

public class NodeValueNodeMap extends NodeValue {
    protected NodeMap binding ;

    public NodeValueNodeMap(NodeMap binding) {
        super();
        this.binding = binding;
    }

    public NodeValueNodeMap(NodeMap binding, Node n) {
        super(n);
        this.binding = binding;
    }

    public NodeMap getBinding() {
        return binding;
    }

    @Override
    protected Node makeNode() {
        return NodeFactory.createLiteralByValue(binding, RDFDatatypeNodeMap.get());
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
                : RDFDatatypeNodeMap.get().unparse(binding);
        return result;
    }

    @Override
    public void visit(NodeValueVisitor visitor) { }
}

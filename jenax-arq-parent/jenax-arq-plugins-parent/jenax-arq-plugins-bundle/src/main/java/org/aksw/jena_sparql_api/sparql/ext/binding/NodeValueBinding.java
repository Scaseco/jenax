package org.aksw.jena_sparql_api.sparql.ext.binding;

import org.aksw.jena_sparql_api.sparql.ext.xml.RDFDatatypeXml;
import org.aksw.jenax.arq.datatype.RDFDatatypeBinding;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueVisitor;

public class NodeValueBinding extends NodeValue {
    protected Binding binding ;

    public NodeValueBinding(Binding binding) {
        super();
        this.binding = binding;
    }

    public NodeValueBinding(Binding binding, Node n) {
        super(n);
        this.binding = binding;
    }

    public Binding getBinding() {
        return binding;
    }

    @Override
    protected Node makeNode() {
        return NodeFactory.createLiteralByValue(binding, RDFDatatypeBinding.get());
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
                : RDFDatatypeXml.get().unparse(binding);
        return result;
    }

    @Override
    public void visit(NodeValueVisitor visitor) { }
}

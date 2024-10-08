package org.aksw.jenax.graphql.sparql.v2.ron;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeVisitor;
import org.apache.jena.graph.Node_Ext;
import org.apache.jena.graph.TextDirection;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.shared.PrefixMapping;

/**
 * Wrapper for Node. Experimental.
 * Intended purpose is to allow for tying additional non-RDF information to a Node.
 * For example, a (Node, RdfObject) pair could be used to extract all outgoing/incoming
 * triples based on the RdfObject.
 */
public abstract class NodeWrapper<T>
    extends Node_Ext<T> // Cannot extend Node directly because its ctor is package private
{
    public NodeWrapper(T object) {
        super(object);
    }

    /** Return the node that is being wrapped. */
    protected abstract Node getDelegate();

    @Override
    public Object visitWith(NodeVisitor v) { return getDelegate().visitWith(v); }

    @Override
    public boolean isConcrete() { return getDelegate().isConcrete(); }

    @Override
    public boolean isLiteral() { return getDelegate().isLiteral(); }

    @Override
    public boolean isBlank() { return getDelegate().isBlank(); }

    @Override
    public boolean isURI() { return getDelegate().isURI(); }

    @Override
    public boolean isVariable() { return getDelegate().isVariable(); }

    @Override
    public boolean isNodeTriple() { return getDelegate().isNodeTriple(); }

    @Override
    public boolean isNodeGraph() { return getDelegate().isNodeGraph(); }

    @Override
    public boolean isExt() { return getDelegate().isExt(); }

    @Override
    public String getBlankNodeLabel() { return getDelegate().getBlankNodeLabel(); }

    @Override
    public LiteralLabel getLiteral() { return getDelegate().getLiteral(); }

    @Override
    public Object getLiteralValue() { return getDelegate().getLiteralValue(); }

    @Override
    public String getLiteralLexicalForm() { return getDelegate().getLiteralLexicalForm(); }

    @Override
    public String getLiteralLanguage() { return getDelegate().getLiteralLanguage(); }

    @Override
    public TextDirection getLiteralTextDirection() { return getDelegate().getLiteralTextDirection(); }

    @Override
    public String getLiteralDatatypeURI() { return getDelegate().getLiteralDatatypeURI(); }

    @Override
    public RDFDatatype getLiteralDatatype() { return getDelegate().getLiteralDatatype(); }

    @Override
    public Object getIndexingValue() { return getDelegate().getIndexingValue(); }

    @Override
    public String getURI() { return getDelegate().getURI(); }

    @Override
    public String getNameSpace() { return getDelegate().getNameSpace(); }

    @Override
    public String getLocalName() { return getDelegate().getLocalName(); }

    @Override
    public String getName() { return getDelegate().getName(); }

    @Override
    public Triple getTriple() { return getDelegate().getTriple(); }

    @Override
    public Graph getGraph() { return getDelegate().getGraph(); }

    @Override
    public boolean hasURI(String uri) { return getDelegate().hasURI(uri); }

    @Override
    public boolean equals(Object o) { return getDelegate().equals(o); }

    @Override
    public boolean sameTermAs(Object o) { return getDelegate().equals(o); }

    @Override
    public boolean sameValueAs(Object o) { return getDelegate().equals(o); }

    @Override
    public String toString() { return getDelegate().toString(); }

    @Override
    public String toString(PrefixMapping pmap) { return getDelegate().toString(pmap); }

    @Override
    public int hashCode() { return getDelegate().hashCode(); }

    @Override
    public boolean matches(Node other) { return getDelegate().equals(other); }

    // ---- Serializable
    // Must be "protected", not "private".
//    protected Object writeReplace() throws ObjectStreamException {
//        Function<Node, Object> function =  Serializer.getNodeSerializer() ;
//        if ( function == null )
//            throw new IllegalStateException("Function for Node.writeReplace not set") ;
//        return function.apply(this);
//    }
//    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//        getDelegate().wr
//    }
//    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
//        throw new IllegalStateException();
//    }
    // ---- Serializable
}

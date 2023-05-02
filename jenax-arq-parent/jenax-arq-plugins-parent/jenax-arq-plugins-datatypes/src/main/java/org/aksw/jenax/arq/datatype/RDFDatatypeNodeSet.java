package org.aksw.jenax.arq.datatype;

import java.util.LinkedHashSet;
import java.util.Set;

import org.aksw.jenax.arq.util.node.NodeSet;
import org.aksw.jenax.arq.util.node.NodeSetImpl;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;

/** Datatype for 'arrays' of RDF terms */
public class RDFDatatypeNodeSet
    extends BaseDatatype
{
    public static final String IRI = RDF.uri + "set";
    public static final RDFDatatypeNodeSet INSTANCE = new RDFDatatypeNodeSet();

    public static RDFDatatype get() {
        return INSTANCE;
    }

    public RDFDatatypeNodeSet() {
        this(IRI);
    }

    public RDFDatatypeNodeSet(String uri) {
        super(uri);
    }

    @Override
    public Class<?> getJavaClass() {
        return NodeSet.class;
    }

    /** Unparse a node list as a string */
    @Override
    public String unparse(Object nodes) {
        String result;
        if (nodes instanceof NodeSet) {
            NodeSet nl = (NodeSet)nodes;
            result = NodeUtils.strNodesWithUndef(NodeUtils.ntFormatter::format, nl.toArray(new Node[0]));
        } else {
            throw new DatatypeFormatException("Not a NodeList datatype");
        }
        return result;
    }

    /** Parse a string as an arbitrary function and extract the arguments as an ExprList */
    @Override
    public NodeSet parse(String str) {
        Set<Node> nodes = NodeUtils.parseNodes(str, new LinkedHashSet<>());
        NodeSet result = new NodeSetImpl(nodes);
        return result;
    }
}

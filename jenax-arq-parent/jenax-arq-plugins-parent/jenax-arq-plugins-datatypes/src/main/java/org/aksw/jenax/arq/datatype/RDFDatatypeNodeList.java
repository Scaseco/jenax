package org.aksw.jenax.arq.datatype;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;

/** Datatype for 'arrays' of RDF terms */
public class RDFDatatypeNodeList
    extends BaseDatatype
{
    public static final String IRI = RDF.uri + "array";
    public static final RDFDatatypeNodeList INSTANCE = new RDFDatatypeNodeList();

    public static RDFDatatype get() {
        return INSTANCE;
    }

    public RDFDatatypeNodeList() {
        this(IRI);
    }

    public RDFDatatypeNodeList(String uri) {
        super(uri);
    }

    @Override
    public Class<?> getJavaClass() {
        return NodeList.class;
    }

    /** Unparse a node list as a string */
    @Override
    public String unparse(Object nodes) {
        String result;
        if (nodes instanceof NodeList) {
            NodeList nl = (NodeList)nodes;
            result = NodeUtils.strNodesWithUndef(NodeUtils.ntFormatter::format, nl.toArray(new Node[0]));
        } else {
            throw new DatatypeFormatException("Not a NodeList datatype");
        }
        return result;
    }

    /** Parse a string as an arbitrary function and extract the arguments as an ExprList */
    @Override
    public NodeList parse(String str) {
        List<Node> nodes = NodeUtils.parseNodes(str, new ArrayList<>());
        NodeList result = new NodeListImpl(nodes);
        return result;
    }

}

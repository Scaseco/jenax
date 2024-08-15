package org.aksw.jenax.arq.datatype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.aksw.jenax.arq.util.node.NodeMap;
import org.aksw.jenax.arq.util.node.NodeMapImpl;
import org.aksw.jenax.norse.term.core.NorseTerms;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * A datatype for storing SPARQL expressions in RDF literals.
 *
 * @author Claus Stadler
 *
 */
public class RDFDatatypeNodeMap
    extends BaseDatatype
{
    public static final String IRI = NorseTerms.NS + "nodeMap"; // "http://jsa.aksw.org/dt/sparql/nodeMap";
    public static final RDFDatatypeNodeMap INSTANCE = new RDFDatatypeNodeMap();

    public static RDFDatatype get() {
        return INSTANCE;
    }

    public RDFDatatypeNodeMap() {
        this(IRI);
    }

    public RDFDatatypeNodeMap(String uri) {
        super(uri);
    }

    @Override
    public Class<?> getJavaClass() {
        return NodeMap.class;
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        String result = value instanceof NodeMap
                ? unparse((NodeMap)value)
                : null;

        return result;
    }

    public static String unparse(NodeMap nodeMap) {
        ExprList el = new ExprList();
        nodeMap.forEach((v, n) -> {
            if (n == null) {
                el.add(NodeValue.makeString(v));
            } else {
                el.add(new E_Equals(NodeValue.makeString(v), NodeValue.makeNode(n)));
            }
        });

        String result = RDFDatatypeExprList.unparse(el);
        return result;
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public NodeMap parse(String lexicalForm) throws DatatypeFormatException {
        return parseCore(lexicalForm);
    }

    // It seems there is no method in Jena's SSE for serializing bindings
    public static NodeMap parseCore(String lexicalForm) {
        // BindingBuilder builder = BindingFactory.builder(parent);
        Map<String, Node> map = new HashMap<>();
        ExprList el = RDFDatatypeExprList.parse(lexicalForm);
        for (Expr e : el) {
            NodeValue k;
            NodeValue v;
            if (e.isConstant()) {
                k = e.getConstant();
                v = null;
            } else {
                E_Equals x = (E_Equals)e;
                k = x.getArg1().getConstant();
                v = x.getArg2().getConstant();
    //            Var v = x.getArg1().asVar();
    //            Node n = x.getArg2().getConstant().asNode();
            }
            String str = k.getString();
            Node node = v == null ? null : v.asNode();
            map.put(str, node);
        }
        NodeMap result = new NodeMapImpl(Collections.unmodifiableMap(map));
        return result;
    }

    public static NodeMap extract(Node node) {
        Object o = node.getLiteralValue();
        NodeMap result = o instanceof NodeMap ? (NodeMap)o : null;
        return result;
    }
}

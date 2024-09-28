package org.aksw.jenax.arq.util.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.NodeCmp;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

public class NodeUtils {

    public static final NodeFormatter ntFormatter = new NodeFormatterNT();
    public static final NodeFormatter ttlFormatter = new NodeFormatterTTL();

    /** Placeholder constants to denote a 'null' node - the absence of a value */
    public static final String nullUri = "http://null.null/null";
    public static final Node nullUriNode = NodeFactory.createURI(nullUri);

    public static final String ANY_IRI_STR = "urn:x-jenax:any";
    public static final Node ANY_IRI = NodeFactory.createURI(ANY_IRI_STR);

    /** Constants for use with {@link #getDatatypeIri(Node)}
        Conflates the term type into a datatype: Under this perspective, IRIs and
        blank nodes are simply literals with the rr:IRI or rr:BlankNode datatype.
        This approach simplifies analytics for e.g. application in schema mapping */
    public static final String R2RML_NS 					= "http://www.w3.org/ns/r2rml#";
    public static final String R2RML_IRI 					= R2RML_NS + "IRI";
    public static final String R2RML_BlankNode 				= R2RML_NS + "BlankNode";


    /** Util method for use a sparql function - <pre>{@code<java:org.aksw.jenax.arq.util.node#hashCode>(?x)}</pre> */
    public static int hashCode(Node node) {
        int result = node == null ? 0 : node.hashCode();
        return result;
    }

    /** Compare nodes via {@link NodeValue#compareAlways(NodeValue, NodeValue)} */
    public static int compareAlways(Node o1, Node o2) {
        int result;
        try {
            result = o1 == null
                ? o2 == null ? 0 : -1
                : o2 == null ? 1 : NodeValue.compareAlways(NodeValue.makeNode(o1), NodeValue.makeNode(o2));
        } catch (Exception e) {
            // RDF terms with mismatch in lexical value / datatype cause an exception
            result = NodeCmp.compareRDFTerms(o1, o2);
        }
        return result;
    }


    /** Filter an iterable of nodes to the set of contained bnodes */
    public static Set<Node> getBnodesMentioned(Iterable<Node> nodes) {
        Set<Node> result = Streams.stream(nodes)
                .filter(Objects::nonNull)
                .filter(Node::isBlank)
                .collect(Collectors.toSet());

        return result;
    }

    /** Filter an iterable of nodes to the set of contained variables */
    public static Set<Var> getVarsMentioned(Iterable<Node> nodes)
    {
        Set<Var> result = Streams.stream(nodes)
                .filter(Objects::nonNull)
                .filter(Node::isVariable)
                .map(node -> (Var)node)
                .collect(Collectors.toSet());

        return result;
    }

    public static boolean isNullOrAny(Node node) {
        return node == null || Node.ANY.equals(node);
    }

    /** Method now exists in Jena's NodeUtils.
     * This method was private in {@link Triple} at least in jena 3.16
     */
    @Deprecated
    public static Node nullToAny(Node n) {
        return org.apache.jena.sparql.util.NodeUtils.nullToAny(n);
    }

    public static Node anyToNull(Node n) {
        return Node.ANY.equals(n) ? null : n;
    }

    /** Method to canonicalize variables to Node.ANY */
    public static Node nullOrFluentToAny(Node n) {
        return n == null || !n.isConcrete() ? Node.ANY : n;
    }

    /** Similar to {@link #nullOrFluentToAny(Node)} but specifically for variables. */
    public static Node nullOrVarToAny(Node node) {
        if ( node == null || node.isVariable() )
            return Node.ANY;
        return node;
    }


    /**
     * Create a logical conjunction of two nodes:
     * - Node.ANY, null or a variable matches everything
     * - If any argument matches everything return the other argument (convert null to ANY)
     * - if both arguments are concrete nodes then return one if them if they are equal
     * - otherwise return null
     *
     */
    public static Node logicalAnd(Node pattern, Node b) {
        Node result = NodeUtils.isNullOrAny(pattern) || pattern.isVariable()
                ? nullToAny(b)
                : NodeUtils.isNullOrAny(b) || Objects.equals(pattern, b)
                    ? nullToAny(pattern)
                    : null;

        return result;
    }

    /**
     * Return the language of a node or null if the argument is not applicable
     *
     * @param node
     * @return
     */
    public static String getLang(Node node) {
        String result = node != null && node.isLiteral() ? node.getLiteralLanguage() : null;
        return result;
    }

    /** If the argument is an IRI-node return the IRI - otherwise return null. Argument may be null. */
    public static String getIriOrNull(Node node) {
        return node == null
            ? null
            : node.isURI() ? node.getURI() : null;
    }

    /**
     * Return a Node's datatype. Thereby, IRIs are returned as rr:IRI and BlankNodes as rr:BlankNode
     * Returns null for variables and unknown node types
     *
     * @param node
     */
    public static String getDatatypeIri(Node node) {
        String result;
        if (node == null) {
            result = null;
        } else if (node.isURI()) {
            result = R2RML_IRI;
        } else if (node.isBlank()) {
            result = R2RML_BlankNode;
        } else if (node.isLiteral()) {
            result = node.getLiteralDatatypeURI();
        } else {
            // Should not happen
            result = null;
        }

        return result;
    }

    /** Create a typed literal for a java object by consulting jena's type mapper */
    public static Node createTypedLiteral(TypeMapper typeMapper, Object o) {
        Class<?> clazz = o.getClass();
        RDFDatatype dtype = typeMapper.getTypeByClass(clazz);
        String lex = dtype.unparse(o);
        Node result = NodeFactory.createLiteral(lex, dtype);
        return result;
    }

    public static Node createUriOrNull(String uri) {
        Node result = uri == null ? null : NodeFactory.createURI(uri);
        return result;
    }

    public static List<Node> fromUris(Iterable<String> uris) {
        List<Node> result = new ArrayList<Node>(Iterables.size(uris));
        for(String uri : uris) {
            Node node = NodeFactory.createURI(uri);
            result.add(node);
        }
        return result;
    }

    public static List<Node> createLiteralNodes(Iterable<String> strings) {
        return Streams.stream(strings).map(NodeFactory::createLiteral).collect(Collectors.toList());
    }

    public static Number getNumberNullable(Node node) {
        Number result = null;
        if (node != null) {
            Object obj = node.getLiteralValue();
            if (!(obj instanceof Number)) {
                throw new RuntimeException("Value is not returned as a number");
            }
            result = ((Number)obj);
        }

        return result;
    }

    public static Number getNumber(Node node) {
        Number number = getNumberNullable(node);
        Objects.requireNonNull(number, "Number expected but got null");
        return number;
    }

    /** Variant of {@link NodeFmtLib#strNodesNT(Node...)} that yield nul as UNDEF */
    public static String strNodesWithUndef(BiConsumer<IndentedWriter, Node> output, Node...nodes) {
        IndentedLineBuffer sw = new IndentedLineBuffer();
        boolean first = true;
        for ( Node n : nodes ) {
            if ( !first )
                sw.append(" ");
            first = false;
            if ( n == null ) {
                sw.append("UNDEF");
                continue;
            }
            output.accept(sw, n);
        }
        return sw.toString();
    }

    public static Node parseNode(String str) {
        Node result = null;
        // NodeFmtLib.strNodes encodes labels - so we need to decode them
        LabelToNode decoder = LabelToNode.createUseLabelEncoded();

        Tokenizer tokenizer = TokenizerText.create().fromString(str).build();
        if (tokenizer.hasNext()) {
            Token token = tokenizer.next() ;
            Node node = token.asNode() ;
//            if ( node == null )
//                throw new RiotException("Bad RDF Term: " + str) ;

            if (node != null) {
                if (node.isBlank()) {
                    String label = node.getBlankNodeLabel();
                    node = decoder.get(null, label);
                }
            }

            result = node;
        }

        return result;
    }

    /** Parse a sequence of nodes into the provided collection */
    public static <C extends Collection<? super Node>> C parseNodes(String str, C segments) {
        // NodeFmtLib.strNodes encodes labels - so we need to decode them
        LabelToNode decoder = LabelToNode.createUseLabelEncoded();

        Tokenizer tokenizer = TokenizerText.create().fromString(str).build();
        while (tokenizer.hasNext()) {
            Token token = tokenizer.next() ;
            Node node = token.asNode() ;
//            if ( node == null )
//                throw new RiotException("Bad RDF Term: " + str) ;

            if (node != null) {
                if (node.isBlank()) {
                    String label = node.getBlankNodeLabel();
                    node = decoder.get(null, label);
                }
            }

            segments.add(node);
        }

        return segments;
    }


    /** Returns the default graph for null or a blank string or 'default' (inoring case),
     * otherwise creates an IRI from the argument */
    public static Node createGraphNode(String graphName) {
        Node result = graphName == null || graphName.isBlank() || graphName.equalsIgnoreCase("default")
                ? Quad.defaultGraphIRI
                : NodeFactory.createURI(graphName);
        return result;
    }

    /** Serialize and deserialize (print/parse) a node. Returns true iff the result is the same (term-equality) as the input. */
    public static boolean isValid(Node node) {
        boolean result = false;
        try {
            validate(node);
            result = true;
        } catch (Exception e) {
            // Ignore
        }
        return result;
    }

    /** Throws an {@link IllegalArgumentException} if {@link #isValid(Node)} returns false. */
    public static void validate(Node node) {
        List<Node> nodes;
        try {
            String str = NodeFmtLib.strNT(node);
            nodes = parseNodes(str, new ArrayList<>());
        } catch (Exception e) {
            throw new ExprEvalException("Node " + node + " did not print-parse");
        }
        if (nodes.size() == 1) {
            Node actual = nodes.get(0);
            if (!node.equals(actual)) { // NodeCmp.compareRDFTerms(node, actual) == 0;
                throw new ExprEvalException("Node " + node + " print-parsed into " + actual);
            }
        } else {
            throw new ExprEvalException("Node " + node + " did not print-parse");
        }
    }

    public static boolean put(BindingBuilder builder, Node nodeOrVar, Node node) {
        boolean result = true;
        if (NodeUtils.isNullOrAny(node)) {
            // nothing to do
        } else if (nodeOrVar.isVariable()) {
            builder.add((Var)nodeOrVar, node);
        } else {
            if (!nodeOrVar.equals(node)) {
                result = false;
            }
        }
        return result;
    }

    public static String getIriOrString(Node node) {
        String result = node == null
                ? null
                : node.isURI()
                    ? node.getURI()
                    : node.isLiteral() && (
                            org.apache.jena.sparql.util.NodeUtils.isSimpleString(node) ||
                            org.apache.jena.sparql.util.NodeUtils.isLangString(node))
                        ? node.getLiteralLexicalForm()
                        : null;
        if (result == null) {
            NodeValue.raise(new ExprTypeException("datatype: Neither IRI nor string: " + node));
        }
        return result;
    }


    /**
     * Returns the "unquoted form" of any Node depending on its type:
     * <ul>
     *   <li>null &rarr; ""</li>
     *   <li>literal &rarr; node.getLiteralLexicalForm()</li>
     *   <li>iri &rarr; node.getURI()</li>
     *   <li>bnode &rarr; node.getBlankNodeLabel()</li>
     *   <li>variable &rarr; node.getName()</li>
     *   <li>otherwise &rarr; NodeFmtLib.displayStr(node) </li>
     * </ul>
     */
    public static String getUnquotedForm(Node node) {
        String result = node == null
                ? ""
                : node.isLiteral()
                    ? node.getLiteralLexicalForm()
                    : node.isURI()
                        ? node.getURI()
                        : node.isBlank()
                            ? node.getBlankNodeLabel()
                            : node.isVariable()
                                ? node.getName()
                                : NodeFmtLib.displayStr(node);
        return result;
    }
}

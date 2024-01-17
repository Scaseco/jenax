package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Objects;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathEvaluationResult;
import javax.xml.xpath.XPathEvaluationResult.XPathResultType;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;

import org.aksw.jena_sparql_api.sparql.ext.url.JenaUrlUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.rdfhdt.hdt.iterator.utils.Iter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

public class JenaXmlUtils {

    private static final Logger logger = LoggerFactory.getLogger(JenaXmlUtils.class);

    public static NodeValue makeXmlNodeValue(Node node) {
        org.w3c.dom.Node xmlNode = extractXmlNode(node);
        if (xmlNode == null) {
            throw new RuntimeException("Not an xml node: " + node);
        }

        return new NodeValueXml(xmlNode);
    }

    public static org.w3c.dom.Node extractXmlNode(NodeValue nv) {
        org.w3c.dom.Node result;
        if (nv instanceof NodeValueXml) {
            result = ((NodeValueXml)nv).getXmlNode();
        } else {
            Node node = nv.getNode();
            result = extractXmlNode(node);
        }

        return result;
    }

    public static org.w3c.dom.Node extractXmlNode(Node node) {
        org.w3c.dom.Node result = null;
        if (node != null && node.isLiteral()) {
            Object value = node.getLiteralValue();

            if (value instanceof org.w3c.dom.Node) {
                result = (org.w3c.dom.Node)value;
            }
        }
        return result;
    }

    public static String toString(org.w3c.dom.Node xmlNode)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            toText(xmlNode, baos);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }

        String result = baos.toString();
        return result;
    }

    public static void toText(org.w3c.dom.Node xmlNode, OutputStream out)
        throws TransformerFactoryConfigurationError, TransformerException
    {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source source = new DOMSource(xmlNode);
        Result output = new StreamResult(out);
        transformer.transform(source, output);
    }

    public static NodeValue parse(InputStream in, RDFDatatypeXml dtype) {
        NodeValue result;
        try {
            Document xmlNode = dtype.getDocumentBuilder().parse(in);
            result = new NodeValueXml(xmlNode);
        } catch (Exception e) {
            throw new QueryExecException("Failed to parse xml from input stream");
        }
        return result;
    }

    public static NodeValue resolve(NodeValue nv, FunctionEnv env) throws Exception {
        RDFDatatypeXml dtype = (RDFDatatypeXml)TypeMapper.getInstance().getTypeByClass(org.w3c.dom.Node.class);

        NodeValue result;
        try (InputStream in = JenaUrlUtils.openInputStream(nv, env)) {
            if (in != null) {
                result = JenaXmlUtils.parse(in, dtype);
            } else {
                throw new ExprEvalException("Failed to obtain text from node " + nv);
            }
        }

        return result;
    }

    public static Iterator<Node> iterateAsJenaNodes(XPathEvaluationResult<?> er, RDFDatatype xmlDatatype) {
        Iterator<Node> result = null;
        XPathResultType type = er.type();
        Object value = er.value();
        Class<?> cls = value.getClass();

        TypeMapper tm = TypeMapper.getInstance();

        switch (type) {
        case BOOLEAN:
        case NUMBER:
        case STRING:
            RDFDatatype dtype = tm.getTypeByClass(cls);
            result = Iter.single(NodeFactory.createLiteralByValue(value, dtype));
            break;
        case NODE:
            throw new IllegalStateException("Not implemented yet");
        case NODESET:
            XPathNodes xmlNodes = (XPathNodes)value;
            result = Iter.map(xmlNodes, JenaXmlUtils::toJenaNode);
            break;
        case ANY:
        default:
            throw new IllegalStateException("Should never come here: Result type was: " + type);
        }

        return result;
    }

    public static Node toJenaNode(org.w3c.dom.Node xmlNode) {
        Node result;
//		if (result != null) {
//			throw new IllegalArgumentException("Node set must contain at most one value");
//		}

        if (xmlNode instanceof Attr) {
            Attr attr = (Attr)xmlNode;
            result = NodeFactory.createLiteral(attr.getValue());
        } else if (xmlNode instanceof Text) {
            Text text = (Text)xmlNode;
            result = NodeFactory.createLiteral(text.getData());
        } else {
            result = NodeFactory.createLiteralByValue(xmlNode, RDFDatatypeXml.get());
        }
        return result;
    }

    public static Iterator<Node> evalXPath(XPathFactory xPathFactory, String queryStr, org.w3c.dom.Node xmlNode)
            throws XPathExpressionException
    {
        NamespaceResolver namespaceResolver = new NamespaceResolver(xmlNode);

        // If 'xml' is a Document with a single node, use the node as the context for the xpath evaluation
        // Rationale: Nodes matched by xml:unnest will be wrapped into new (invisible) XML documents
        // We would expect to be able to run xpath expressions directly on the
        // result nodes of the unnesting - without having to consider the invisible document root node
        if(xmlNode instanceof Document && xmlNode.getChildNodes().getLength() == 1) {
            xmlNode = xmlNode.getFirstChild();
        }

        XPath xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(namespaceResolver);
        XPathExpression expr = xPath.compile(queryStr);

        XPathEvaluationResult<?> er = expr.evaluateExpression(xmlNode);
        Iterator<org.apache.jena.graph.Node> result = JenaXmlUtils.iterateAsJenaNodes(er, RDFDatatypeXml.INSTANCE);

        return result;
    }

    public static QueryIterator evalXPath(
            XPathFactory xPathFactory,
            Binding binding, ExecutionContext execCxt,
            Node node, Node xpathNode, Var outputVar) {

        // Obtain the datatype object in order to create rdf Nodes from dom Nodes
        // TODO We may want to check for this datatype in the execution context
        // before resorting to the type mapper
        RDFDatatype xmlDatatype = TypeMapper.getInstance().getTypeByClass(org.w3c.dom.Node.class);
        Objects.requireNonNull(xmlDatatype);

        QueryIterator result = null;

        org.w3c.dom.Node xmlNode = JenaXmlUtils.extractXmlNode(node);
        if(xmlNode != null) {

            Object queryObj = xpathNode.isLiteral() ? xpathNode.getLiteralValue() : null;
            String queryStr = queryObj instanceof String ? (String)queryObj : null;

            if(queryStr != null) {
                // List<Binding> bindings = new ArrayList<Binding>();

                try {
                    Iterator<Node> nodeIt = JenaXmlUtils.evalXPath(xPathFactory, queryStr, xmlNode);
                    Iterator<Binding> bindingIt = Iter.map(nodeIt, jenaNode -> {

                        // For some reason xpath evaluation on prior xpath results does not yield expected results
                        // Therefore 'clone' (by means of serialize/parse) the result into an independent document)
                        // Commenting the following two lines out causes at least one test to fail.
                        org.w3c.dom.Node reparsedXmlNode = (org.w3c.dom.Node)xmlDatatype.parse(jenaNode.getLiteralLexicalForm());
                        Node reparsedNode = NodeFactory.createLiteralByValue(reparsedXmlNode, xmlDatatype);

                        Binding b = BindingFactory.binding(binding, outputVar, reparsedNode);
                        return b;
                    });

                    result = QueryIterPlainWrapper.create(bindingIt, execCxt);
                } catch(Exception e) {
                    logger.warn(e.getLocalizedMessage());
                }
            }
        }

        if(result == null) {
            result = QueryIterNullIterator.create(execCxt);
        }

        return result;
    }
}

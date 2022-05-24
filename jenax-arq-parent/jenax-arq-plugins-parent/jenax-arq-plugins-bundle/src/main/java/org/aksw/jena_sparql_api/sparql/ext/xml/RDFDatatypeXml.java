package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.vocabulary.XSD;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RDFDatatypeXml
	extends BaseDatatype
{
	public static final RDFDatatypeXml INSTANCE = new RDFDatatypeXml();

	public static final String IRI = XSD.NS + "xml";

    protected DocumentBuilder documentBuilder;


    public static DocumentBuilder createDefaultDocumentBuilder() {
    	DocumentBuilder result;
		try {
//			result = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setValidating(false);
	        factory.setNamespaceAware(true);

	        result = factory.newDocumentBuilder();
	        result.setEntityResolver(new EntityResolver() {

	            @Override
	            public InputSource resolveEntity(String publicId, String systemId)
	                    throws SAXException, IOException {
	                //System.out.println("Ignoring " + publicId + ", " + systemId);
	                return new InputSource(new StringReader(""));
	            }
	        });
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}

    	return result;
    }

    public RDFDatatypeXml() {
        this(createDefaultDocumentBuilder());
    }

    public RDFDatatypeXml(DocumentBuilder documentBuilder) {
        this(IRI, documentBuilder);
    }

    public RDFDatatypeXml(String uri, DocumentBuilder documentBuilder) {
        super(uri);
    	this.documentBuilder = documentBuilder;
    }

//    public RDFDatatypeXml(String uri, Gson gson) {
//        super(uri);
//        this.gson = gson;
//    }

    @Override
    public Class<?> getJavaClass() {
        return Node.class;
    }

    @Override
    public boolean isValidValue(Object valueForm) {
    	boolean isValid = valueForm instanceof Node;
    	return isValid;
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
    	Node node = (Node)value;
    	String result = JenaXmlUtils.toString(node);
    	return result;
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public synchronized Node parse(String lexicalForm) throws DatatypeFormatException {
        Document result;
		try {
			result = documentBuilder.parse(new InputSource(new StringReader(lexicalForm)));
		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}

        return result;
    }

    public DocumentBuilder getDocumentBuilder() {
		return documentBuilder;
	}

}

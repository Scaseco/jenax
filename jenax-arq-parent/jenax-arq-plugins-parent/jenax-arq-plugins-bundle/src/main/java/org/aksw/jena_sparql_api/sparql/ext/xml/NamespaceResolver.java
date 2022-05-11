package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Node;

public class NamespaceResolver implements NamespaceContext {
    //Store the source document to search the namespaces
    private Node sourceDocument;

    public NamespaceResolver(Node document) {
        sourceDocument = document;
    }

    //The lookup for the namespace uris is delegated to the stored document.
    public String getNamespaceURI(String prefix) {
        if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return sourceDocument.lookupNamespaceURI(null);
        } else {
            return sourceDocument.lookupNamespaceURI(prefix);
        }
    }

    public String getPrefix(String namespaceURI) {
        return sourceDocument.lookupPrefix(namespaceURI);
    }

    public Iterator<String> getPrefixes(String namespaceURI) {
    	return null;
    }
}

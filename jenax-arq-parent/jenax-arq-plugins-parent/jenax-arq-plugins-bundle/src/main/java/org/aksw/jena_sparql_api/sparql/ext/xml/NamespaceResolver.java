package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.util.Iterator;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Node;

import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;

public class NamespaceResolver
	implements NamespaceContext
{
    protected Node xmlNode;

    public NamespaceResolver(Node xmlNode) {
        this.xmlNode = xmlNode;
    }

    //The lookup for the namespace uris is delegated to the stored document.
    public String getNamespaceURI(String prefix) {
    	Iterable<Node> traverser = Traverser.<Node>forTree(xmlNode -> new ListOverNodeList(xmlNode.getChildNodes()))
    		.breadthFirst(xmlNode);

    	String key = XMLConstants.DEFAULT_NS_PREFIX.equals(prefix) ? null : prefix;

    	String result = Streams.stream(traverser)
    		.map(xmlNode -> xmlNode.lookupNamespaceURI(key))
    		.filter(Objects::nonNull)
    		.findFirst()
    		.orElse(null);

    	// String result = xmlNode.lookupNamespaceURI(key);
    	return result;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return xmlNode.lookupPrefix(namespaceURI);
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
    	throw new UnsupportedOperationException();
    	/*
    	String tmp = getPrefix(namespaceURI);
    	Iterator<String> result = tmp == null
    			? Collections.emptyIterator()
    			: Collections.singleton(tmp).iterator();

    	return result;
    	*/
    }
}

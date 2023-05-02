package org.aksw.jena_sparql_api.rx.entity.util;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class IdVocab {
	/**
	 * A property that assigns a resource to an RDF list of individual RDF terms that act as components of a composite id.
	 * <br />
	 * Example:
	 * {@code <urn:s> compositeId (<foo> eg:bar "baz") .}
	 */
	public static final Property compositeId = ResourceFactory.createProperty("http://jsa.aksw.org/id/compositeId");
}

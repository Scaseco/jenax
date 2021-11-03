package org.aksw.jena_sparql_api.views;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;


public class OgcVocab {
	public static final String ns = "http://www.opengis.net/ont/geosparql#";
	
	
	// TODO Make a Jena resource
	public static final String wktLiteral = ns + "wktLiteral";
	
	//public static final Property stIntersects = create

	//public static final String stGeomFromText = ns + "stGeomFromText";
	//public static final String stPoint = ns
	
	
	
	public static Resource createResource(String name) {
		return ResourceFactory.createResource(ns + name);
	}

	public static Property createProperty(String name) {
		return ResourceFactory.createProperty(ns + name);
	}

}

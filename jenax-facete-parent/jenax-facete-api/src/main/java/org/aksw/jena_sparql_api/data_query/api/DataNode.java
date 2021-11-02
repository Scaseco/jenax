package org.aksw.jena_sparql_api.data_query.api;

import java.util.Map;

import org.apache.jena.rdf.model.Property;

public interface DataNode {
	Map<Property, DataNode> getDeclaredOutProperties();
	
	
	
	// Get the outgoing property
	DataNode out(Property property);
	
}
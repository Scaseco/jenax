package org.aksw.facete.v3.bgp.api;

import java.util.Map;

import org.apache.jena.sparql.syntax.Element;

// Not used yet; but it might make sense to add these classes to the faceted search model
public interface Relation {
	// Note: Element needs to be registered on Jena's Type Mapper
	Element getElement();
	
	/**
	 * Mapping of variable names in the element to node in the RDF model
	 * 
	 * Note: Members may be related by a generic predicate such as 'hasNode' as well as sub-properties
	 * such as hasSourceNode and asTargetNode
	 * 
	 * @return
	 */
	Map<String, BgpNode> getNodes();
}

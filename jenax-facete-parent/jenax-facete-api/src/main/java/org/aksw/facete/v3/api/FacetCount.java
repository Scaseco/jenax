package org.aksw.facete.v3.api;

import org.aksw.commons.util.range.CountInfo;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * Class used for query results of facet counts.
 * Additional information may be accessible via the Resource API.
 * 
 * @author raven
 *
 */
public interface FacetCount
	extends Resource
{
	//String getPredicate();
	Node getPredicate();
	
	// Maybe in the future allow paths
	//Path getPath();
	CountInfo getDistinctValueCount();
}

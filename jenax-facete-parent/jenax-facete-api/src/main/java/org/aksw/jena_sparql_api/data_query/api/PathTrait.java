package org.aksw.jena_sparql_api.data_query.api;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;


/**
 * Trait for navigation along paths based on RDF predicates.
 * Implementations should not use this interface directly; instead use
 * {@link PathTraitNode} and {@link PathTraitString} for {@link Node} and {@link String} backed versions.
 * 
 * @author Claus Stadler, Oct 8, 2018
 *
 * @param <T>
 */
public interface PathTrait<T> {
	T fwd(Resource p);
	T fwd(Node p);
	T fwd(String p);
	
	T bwd(Resource p);
	T bwd(Node p);
	T bwd(String p);

	T step(Resource p, boolean isFwd);
	T step(Node p, boolean isFwd);
	T step(String p, boolean isFwd);
}

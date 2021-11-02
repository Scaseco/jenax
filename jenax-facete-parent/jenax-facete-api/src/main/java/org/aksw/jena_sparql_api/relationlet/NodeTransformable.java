package org.aksw.jena_sparql_api.relationlet;

import org.apache.jena.sparql.graph.NodeTransform;

/**
 * Interface that introduces an applyNodeTransform method.
 * Objects of implementing classes are expected to return a copy of themselves with the
 * substitution applied.
 *
 * Not yet used, but may be useful for substitutions
 * 
 * @author raven
 *
 * @param <T>
 */
public interface NodeTransformable<T> {
	T applyNodeTransform(NodeTransform nodeTransform);
}

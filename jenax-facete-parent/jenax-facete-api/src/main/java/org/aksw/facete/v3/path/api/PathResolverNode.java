package org.aksw.facete.v3.path.api;

import org.aksw.facete.v3.api.traversal.TraversalNode;

/**
 * A path resolver intended for traversing RDF predicates.
 * 
 * Note: This class differs from the PathResolver in the mapper-module:
 * - This class supports aliases and direction
 * - TODO Investigate whether the other PathResolver could be aligned with this one
 * 
 * @author raven
 *
 */
public interface PathResolverNode<T>
	extends TraversalNode<PathResolverNode<T>, PathResolverDirNode<T>, PathResolverMultiNode<T>>
{
	T getResult();
}

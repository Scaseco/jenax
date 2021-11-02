package org.aksw.facete.v3.experimental;

import org.aksw.facete.v3.api.traversal.TraversalDirNode;
import org.aksw.facete.v3.api.traversal.TraversalMultiNode;
import org.aksw.facete.v3.api.traversal.TraversalNode;
import org.apache.jena.rdf.model.Resource;

public class PathFactoryMultiNode<N extends TraversalNode<N,D,M>, D extends TraversalDirNode<N, M>, M extends TraversalMultiNode<N>>
	extends PathMultiNode<N, D, M>
{
	protected Factory<N, D, M> factory;
	
	
	public PathFactoryMultiNode(D parent, Resource property, Factory<N, D, M> factory) {
		super(parent, property);
		this.factory = factory;
	}


	@Override
	protected N viaImpl(String alias) {
		@SuppressWarnings("unchecked")
		N result = factory.newNode((M)this, alias);
		return result;
	}
}
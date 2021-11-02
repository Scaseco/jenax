package org.aksw.facete.v3.experimental;

import org.aksw.facete.v3.api.traversal.TraversalDirNode;
import org.aksw.facete.v3.api.traversal.TraversalMultiNode;
import org.aksw.facete.v3.api.traversal.TraversalNode;
import org.apache.jena.rdf.model.Resource;

interface Factory<N extends TraversalNode<N,D,M>, D extends TraversalDirNode<N, M>, M extends TraversalMultiNode<N>> {

	D newDirNode(N node, boolean isFwd);
	M newMultiNode(D dirNode, Resource property);
	N newNode(M multiNode, String alias);
}


class PathFactoryNode<N extends TraversalNode<N,D,M>, D extends TraversalDirNode<N, M>, M extends TraversalMultiNode<N>>
	extends PathNode<N, D, M>
{
	protected Factory<N, D, M> factory;

	@Override
	public D create(boolean isFwd) {
		D result = factory.newDirNode((N)this, isFwd);
		return result;
	}	
}

class PathFactoryDirNode<N extends TraversalNode<N,D,M>, D extends TraversalDirNode<N, M>, M extends TraversalMultiNode<N>>
	extends PathDirNode<N, M>
{
	protected Factory<N, D, M> factory;

	public PathFactoryDirNode(N parent, boolean isFwd, Factory<N, D, M> factory) {
		super(parent, isFwd);
		this.factory = factory;
	}

	@Override
	protected M viaImpl(Resource property) {
		M result = factory.newMultiNode((D)this, property);
		return result;
	}
}

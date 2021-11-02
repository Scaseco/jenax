package org.aksw.facete.v3.experimental;

import org.aksw.facete.v3.api.traversal.TraversalDirNode;
import org.aksw.facete.v3.api.traversal.TraversalMultiNode;
import org.aksw.facete.v3.api.traversal.TraversalNode;

public abstract class PathNode<N extends TraversalNode<N,D,M>, D extends TraversalDirNode<N, M>, M extends TraversalMultiNode<N>>
	implements TraversalNode<N, D, M>
{
	protected M parent;
	protected String alias;
	
	public PathNode() {
		this(null, null);
	}

	public PathNode(M parent, String alias) {
		super();
		this.parent = parent;
		this.alias = alias;
	}

	public M parent() {
		return parent;
	}
	
	@Override
	public D fwd() {
		return create(true);
	}

	@Override
	public D bwd() {
		return create(false);
	}
	
	public abstract D create(boolean isFwd);
}
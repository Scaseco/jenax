package org.aksw.facete.v3.experimental;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.facete.v3.api.traversal.TraversalDirNode;
import org.aksw.facete.v3.api.traversal.TraversalMultiNode;
import org.apache.jena.rdf.model.Resource;

public abstract class PathDirNode<N, M extends TraversalMultiNode<N>>
	implements TraversalDirNode<N, M>
{
	protected N parent;
	protected boolean isFwd;
	protected Map<Resource, M> propToMultiNode = new LinkedHashMap<>();

	public PathDirNode(N parent, boolean isFwd) {
		super();
		this.parent = parent;
		this.isFwd = isFwd;
	}
	
	@Override
	public boolean isFwd() {
		return isFwd;
	}

	@Override
	public M via(Resource property) {
		M result = propToMultiNode.computeIfAbsent(property, p -> {
			// Expanded for easier debugging
			return viaImpl(p); 
		});
		return result;
	}

	protected abstract M viaImpl(Resource property);
}
package org.aksw.facete.v3.experimental;

import org.apache.jena.rdf.model.Resource;

public class PathBuilderDirNode
	extends PathDirNode<PathBuilderNode, PathBuilderMultiNode>
{
	public PathBuilderDirNode(PathBuilderNode parent, boolean isFwd) {
		super(parent, isFwd);
	}

	@Override
	protected PathBuilderMultiNode viaImpl(Resource property) {
		return new PathBuilderMultiNode(this, property);
	}	
}
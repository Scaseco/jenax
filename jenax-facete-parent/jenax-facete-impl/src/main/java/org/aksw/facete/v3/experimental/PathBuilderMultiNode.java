package org.aksw.facete.v3.experimental;

import org.apache.jena.rdf.model.Resource;

public class PathBuilderMultiNode
	extends PathMultiNode<PathBuilderNode, PathBuilderDirNode, PathBuilderMultiNode>
{
	public PathBuilderMultiNode(PathBuilderDirNode parent, Resource property) {
		super(parent, property);
	}

	@Override
	protected PathBuilderNode viaImpl(String alias) {
		return new PathBuilderNode(this, alias);
	}	
}
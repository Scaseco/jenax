package org.aksw.facete.v3.experimental;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.aksw.facete.v3.api.AliasedPath;
import org.aksw.facete.v3.api.AliasedPathImpl;
import com.google.common.collect.Maps;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;

public class PathBuilderNode
	extends PathNode<PathBuilderNode, PathBuilderDirNode, PathBuilderMultiNode>
{
	public PathBuilderNode(PathBuilderMultiNode parent, String alias) {
		super(parent, alias);
	}


	@Override
	public PathBuilderDirNode create(boolean isFwd) {
		return new PathBuilderDirNode(this, isFwd);
	}
	
	
	public static PathBuilderNode start() {
		return new PathBuilderNode(null, null);
	}
	
	
	public AliasedPath aliasedPath() {
		AliasedPath result;
		if(parent != null) {
			AliasedPath parentPath = parent.parent.parent.aliasedPath();
			
			Node node = parent.property.asNode();
			P_Path0 p = parent.isFwd ? new P_Link(node) : new P_ReverseLink(node);
			Entry<P_Path0, String> step = Maps.immutableEntry(p, alias);
			
			result = parentPath.subPath(step);
		} else {
			result = new AliasedPathImpl(new ArrayList<>());
		}
		return result;
	}
}
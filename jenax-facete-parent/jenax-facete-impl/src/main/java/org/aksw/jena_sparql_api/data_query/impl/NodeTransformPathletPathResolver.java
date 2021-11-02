package org.aksw.jena_sparql_api.data_query.impl;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.pathlet.Path;
import org.aksw.jena_sparql_api.pathlet.PathletJoinerImpl;
import org.aksw.jena_sparql_api.relationlet.VarRefStatic;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeTransform;

/**
 * Scan all nodes of a query for instances of NodePathletPath and
 * add resolve the contained path against a pathletContainer
 * 
 * @author raven
 *
 */
public class NodeTransformPathletPathResolver
	implements NodeTransform
{
	protected PathletJoinerImpl pathletContainer;
	
	public NodeTransformPathletPathResolver(PathletJoinerImpl pathletContainer) {
		this.pathletContainer = pathletContainer;
	}

	// Substitute the node with reference
	@Override
	public Node apply(Node t) {
		Node result = t;
		if(t instanceof NodePathletPath) {
			Path path = ((NodePathletPath)t).getValue();
			Supplier<VarRefStatic> varRefSupplier = pathletContainer.resolvePath(path);
				
			result = new NodeVarRefStaticSupplier(varRefSupplier);
		}
		
		return result;
	}
}

package org.aksw.jena_sparql_api.data_query.impl;

import org.aksw.jena_sparql_api.pathlet.Path;
import org.aksw.jenax.arq.util.node.NodeCustom;

public class NodePathletPath
	extends NodeCustom<Path>
{	
	public NodePathletPath(Path value) {
		super(value);
	}

	public static NodePathletPath create(Path path) {
		return new NodePathletPath(path);
	}
}

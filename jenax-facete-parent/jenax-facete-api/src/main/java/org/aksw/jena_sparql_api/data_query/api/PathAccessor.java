package org.aksw.jena_sparql_api.data_query.api;

import org.apache.jena.graph.Node;

public interface PathAccessor<P>
	extends PathAccessorRdf<P>
{
	Class<P> getPathClass();

	
	/** Try to map to expr to a path */
	//P tryMapToPath(Expr expr);
	P tryMapToPath(Node node);
}

package org.aksw.jena_sparql_api.data_query.api;

public interface PathAccessorSimple<P> {
	P getParent(P path);
}

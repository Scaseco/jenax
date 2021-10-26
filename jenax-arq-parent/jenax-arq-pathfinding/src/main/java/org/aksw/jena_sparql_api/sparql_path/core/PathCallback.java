package org.aksw.jena_sparql_api.sparql_path.core;

import org.aksw.jenax.sparql.path.SimplePath;

public interface PathCallback {
	void handle(SimplePath path);
}
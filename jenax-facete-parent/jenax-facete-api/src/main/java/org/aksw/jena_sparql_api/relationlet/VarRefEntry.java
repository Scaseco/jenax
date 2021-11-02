package org.aksw.jena_sparql_api.relationlet;

import org.apache.jena.sparql.core.Var;

public interface VarRefEntry
	extends VarRef
{
	RelationletEntry<?> getEntry();
	Var getVar();
}
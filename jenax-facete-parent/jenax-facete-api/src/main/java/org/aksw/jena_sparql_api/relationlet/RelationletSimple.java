package org.aksw.jena_sparql_api.relationlet;

import org.apache.jena.sparql.syntax.Element;

public interface RelationletSimple
	extends Relationlet
{
	Element getElement();
//	Relationlet getMember(String alias);
//
//	NestedVarMap getNestedVarMap();
}

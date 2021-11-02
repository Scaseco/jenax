package org.aksw.jena_sparql_api.relationlet;

import org.apache.jena.sparql.syntax.Element;

public class Relationlets {
	public static Relationlet from(Element e) {
		return new RelationletElementImpl(e);
	}
}
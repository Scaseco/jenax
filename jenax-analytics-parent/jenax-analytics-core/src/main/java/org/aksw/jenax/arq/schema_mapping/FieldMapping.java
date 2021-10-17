package org.aksw.jenax.arq.schema_mapping;

import org.aksw.jenax.arq.decisiontree.api.DecisionTreeSparqlExpr;
import org.apache.jena.sparql.core.Var;

public interface FieldMapping {
	Var getVar();
	DecisionTreeSparqlExpr getDefinition();
	String getDatatypeIri();
	boolean isNullable();
}

package org.aksw.jenax.arq.decisiontree.impl.jena;

import java.util.Map;

import org.aksw.jenax.arq.decisiontree.api.DecisionTreeSparqlExpr;
import org.apache.jena.sparql.core.Var;

/**
 * A conditional var definition maps each variable to a
 * decision tree for which condition holds
 * 
 * @author raven
 *
 */
public interface ConditionalVarDefinition {
	ConditionalVarDefinition put(Var var, DecisionTreeSparqlExpr definition);	
	Map<Var, DecisionTreeSparqlExpr> getDefinitions();
}

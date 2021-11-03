package org.aksw.jena_sparql_api.restriction;

import org.aksw.jena_sparql_api.views.RdfTermType;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Interface for associating restrictions with
 * (query) variables.
 * 
 * @author raven
 *
 * @param <V>
 */
public interface RestrictionManager {
	
	/**
	 * Test whether the given expression is satisfiable in
	 * regard to the given restrictions
	 * 
	 * @param expr The expression to test for satisfiability
	 * @return The assessment of the satisfiability
	 */
	Boolean determineSatisfiability(Expr expr);


	/**
	 * Get the restriction associated with the given variable
	 * 
	 * @param a
	 * @return
	 */
	RestrictionImpl getRestriction(Var a);

	/**
	 * Return the restriction associated with as expression.
	 * If the expression is a variable, getRestriction(Var) is invoked.
	 * 
	 * restriction[concat("a", "b")] = prefixset({"ab"}) 
	 * 
	 * @param expr
	 * @return
	 */
	RestrictionImpl getRestriction(Expr expr);
	
	RestrictionImpl getOrCreateLocalRestriction(Var a);

	void stateType(Var a, RdfTermType type);

	void stateNode(Var a, Node b);

	void stateUri(Var a, String uri);

	void stateLiteral(Var a, NodeValue b);

	void stateNonEqual(Var a, Var b);
}

package org.aksw.facete.v3.api;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.Expr;

/**
 * A constraint is an entity which (based on its state) yields a constraint *expression*
 * over the nodes of the facet query.
 * 
 * @author raven
 *
 */
public interface FacetConstraint
	extends Resource
{
	boolean enabled();
	FacetConstraint enabled(boolean onOrOff);
	
	Expr expr();
	FacetConstraint expr(Expr expr);
}

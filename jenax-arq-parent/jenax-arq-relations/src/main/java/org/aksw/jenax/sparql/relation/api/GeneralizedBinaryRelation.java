package org.aksw.jenax.sparql.relation.api;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

/**
 * A relation with multiple source and target variables
 * @author raven
 *
 */
public interface GeneralizedBinaryRelation
	extends Relation
{
	Set<Var> getSourceVars();
	Set<Var> getTargetVars();
}

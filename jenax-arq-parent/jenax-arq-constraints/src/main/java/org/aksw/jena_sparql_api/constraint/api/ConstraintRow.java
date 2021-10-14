package org.aksw.jena_sparql_api.constraint.api;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

/** A constraint backed by a conjunctive mapping of variables to constraints.
 * Whereas a {@link Binding} is a mapping of variables to concrete nodes,
 * this object represents a mapping of variables to their possible values.
 */
public class ConstraintRow
    extends ConjunctiveConstraintMap<Var>
{
}

package org.aksw.jenax.constraint.api;

import java.util.Collection;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

/** A constraint backed by a conjunctive mapping of variables to constraints.
 * Whereas a {@link Binding} is a mapping of variables to concrete nodes,
 * this object represents a mapping of variables to their possible values.
 */
public interface ConstraintRow
    extends Contradictable
{
    ConstraintRow stateIntersection(ConstraintRow that);
    ConstraintRow stateUnion(ConstraintRow that);

    ConstraintRow stateIntersection(Var var, ValueSpace space);
    ConstraintRow stateUnion(Var var, ValueSpace space);

    // Iterator<Var>
    Collection<Var> getVars();
    ValueSpace get(Var var);
}

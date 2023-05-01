package org.aksw.jenax.constraint.api;

import java.util.Collection;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

/**
 * ConstrainedBinding.
 * Whereas a {@link Binding} is a mapping of variables to concrete nodes,
 * this class represents a mapping of variables to their possible values.
 */
public interface CBinding
    extends Contradictable
{
    CBinding stateIntersection(CBinding that);
    CBinding stateUnion(CBinding that);

    CBinding stateIntersection(Var var, VSpace space);
    CBinding stateUnion(Var var, VSpace space);

    CBinding project(Collection<Var> vars);

    CBinding cloneObject();

    Collection<Var> getVars();
    VSpace get(Var var);
}

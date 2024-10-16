package org.aksw.jenax.sparql.fragment.api;

import java.util.List;

import org.apache.jena.sparql.core.Var;

/** A fragment together with a distinguished list of variables which can be used to "connect" fragments.
 *  It is assumed that all vars of the connective are contained in fragment.getVars().
 */
public interface Connective {
    Fragment getFragment();
    List<Var> getVars();
}

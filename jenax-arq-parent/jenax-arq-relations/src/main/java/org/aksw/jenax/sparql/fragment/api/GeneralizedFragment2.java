package org.aksw.jenax.sparql.fragment.api;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

/**
 * A relation with multiple source and target variables
 * @author raven
 *
 */
public interface GeneralizedFragment2
	extends Fragment
{
	Set<Var> getSourceVars();
	Set<Var> getTargetVars();
}

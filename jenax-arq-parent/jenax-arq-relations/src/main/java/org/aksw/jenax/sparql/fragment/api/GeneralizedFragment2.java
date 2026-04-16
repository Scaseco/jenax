package org.aksw.jenax.sparql.fragment.api;

import java.util.List;

import org.apache.jena.sparql.core.Var;

/**
 * A relation with multiple source and target variables
 * @author raven
 *
 */
public interface GeneralizedFragment2
    extends Fragment
{
    List<Var> getSourceVars();
    List<Var> getTargetVars();
}

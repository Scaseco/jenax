package org.aksw.jenax.sparql.fragment.api;

import java.util.List;

import org.apache.jena.sparql.core.Var;

public interface GeneralizedFragment1
    extends Fragment
{
    /** A list of variables that typically forms a composite key. */
    List<Var> getKeyVars();
}

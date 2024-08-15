package org.aksw.jenax.facete.treequery2.api;

import java.util.List;

import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.apache.jena.sparql.core.Var;

/**
 * A generalization of a NodeFragment:
 * A sparql fragment with a declared set of join variables.
 */
public interface RelationFragment {
    public List<Var> getJoinVars();
    // protected EntityTemplate entityTemplate;
    // protected Element element;
    public Fragment getFragment();
}

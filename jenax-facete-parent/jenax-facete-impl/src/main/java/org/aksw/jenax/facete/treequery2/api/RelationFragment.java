package org.aksw.jenax.facete.treequery2.api;

import java.util.List;

import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.sparql.core.Var;

public interface RelationFragment {
    public List<Var> getJoinVars();
    // protected EntityTemplate entityTemplate;
    // protected Element element;
    public Relation getRelation();
}
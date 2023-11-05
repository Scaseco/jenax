package org.aksw.jena_sparql_api.sparql_path.api;


import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.path.SimplePath;

public interface ConceptPathFinder {
    PathSearch<SimplePath> createSearch(Fragment1 sourceConcept, Fragment1 targetConcept);
    //ConceptPathFinder setSource(UnaryRelation source);
    //ConceptPathFinder setTarget(UnaryRelation target);
}

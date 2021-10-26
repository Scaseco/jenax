package org.aksw.jena_sparql_api.sparql_path.api;


import org.aksw.jenax.sparql.path.SimplePath;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;

public interface ConceptPathFinder {
	PathSearch<SimplePath> createSearch(UnaryRelation sourceConcept, UnaryRelation targetConcept);
	//ConceptPathFinder setSource(UnaryRelation source);
	//ConceptPathFinder setTarget(UnaryRelation target);
}
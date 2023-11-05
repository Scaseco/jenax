package org.aksw.jena_sparql_api.entity.graph.metamodel;

import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.apache.jena.graph.Node;

/**
 * A ConceptManager allocates names for concepts and roles.
 * Furthermore, it may support subsumption checking on a best-effort basis.
 *
 *
 * @author raven
 *
 */
public interface ConceptManager {
    Node getOrCreate(Fragment1 concept);
}

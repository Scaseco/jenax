package org.aksw.facete.v4.impl;

import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.graph.Node;

/**
 * Mapping of property nodes (should always be IRI nodes) to a defining Relation.
 */
public interface PropertyResolver {

    /**
     * Resolve a property to its definition.
     * By default, a property P maps to the definition "?s ?o WHERE { ?s P ?o }".
     *
     * @param property The property node for which to carry out a lookup
     * @return The relation that is the result of the lookup. Never null.
     */
    Relation resolve(Node property);
}

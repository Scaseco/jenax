package org.aksw.jenax.graphql.sparql.v2.schema;

import java.util.Collection;
import java.util.Optional;

public interface SchemaNode {
    Optional<SchemaEdge> getEdge(String name);
    Collection<SchemaEdge> listEdges();
    Fragment getFragment();
}

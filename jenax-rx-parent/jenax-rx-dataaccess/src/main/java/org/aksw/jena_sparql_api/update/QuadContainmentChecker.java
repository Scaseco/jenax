package org.aksw.jena_sparql_api.update;

import java.util.Set;

import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.apache.jena.sparql.core.Quad;

public interface QuadContainmentChecker {
    Set<Quad> contains(QueryExecutionFactory qef, Iterable<Quad> quads);
}

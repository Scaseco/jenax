package org.aksw.jena_sparql_api.update;

import java.util.Set;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.apache.jena.sparql.core.Quad;

public interface QuadContainmentChecker {
    Set<Quad> contains(QueryExecutionFactory qef, Iterable<Quad> quads);
}

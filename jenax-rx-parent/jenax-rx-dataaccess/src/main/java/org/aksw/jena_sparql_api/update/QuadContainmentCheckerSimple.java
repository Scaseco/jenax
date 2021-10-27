package org.aksw.jena_sparql_api.update;

import java.util.Set;

import org.aksw.jena_sparql_api.core.QuadContainmentChecker;
import org.apache.jena.sparql.core.Quad;

public class QuadContainmentCheckerSimple
    implements QuadContainmentChecker
{
    @Override
    public Set<Quad> contains(QueryExecutionFactory qef, Iterable<Quad> quads) {
        Set<Quad> result = QuadContainmentUtils.checkContainment(qef, quads);
        return result;
    }
}

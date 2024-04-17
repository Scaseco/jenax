package org.aksw.jena_sparql_api.sparql_path.core;

import java.util.Collection;
import java.util.Collections;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class PathConstraint2
    extends PathConstraintBase
{
    @Override
    public Collection<Triple> createOutgoingPattern(Node type, Node p) {
        Triple t = Triple.create(type, VocabPath.hasOutgoingPredicate.asNode(), p);
        return Collections.singleton(t);
    }

    @Override
    public Collection<Triple> createIngoingPattern(Node type, Node p) {
        Triple u = Triple.create(type, VocabPath.isIngoingPredicateOf.asNode(), type);
        return Collections.singleton(u);
    }

}

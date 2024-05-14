package org.aksw.jena_sparql_api.sparql_path.core;

import java.util.Arrays;
import java.util.Collection;

import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.OWL2;

public class PathConstraint3
    extends PathConstraintBase
{
    @Override
    public Collection<Triple> createOutgoingPattern(Node type, Node p) {
        // TODO: Rename Vars.p if it is equal to s or p

        return Arrays.asList(
                Triple.create(type, Vars.x, Vars.o),
                Triple.create(Vars.x, OWL2.annotatedProperty.asNode(), p));
    }

    @Override
    public Collection<Triple> createIngoingPattern(Node type, Node p) {
        return Arrays.asList(
                Triple.create(Vars.s, Vars.x,type),
                Triple.create(Vars.x, OWL2.annotatedProperty.asNode(), p));

//		Triple u = Triple.create(p, VocabPath.isIngoingPredicateOf.asNode(), type);
//    	return Collections.singleton(u);
    }

}

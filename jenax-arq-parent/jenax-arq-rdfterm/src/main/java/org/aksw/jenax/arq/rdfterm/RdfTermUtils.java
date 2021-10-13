package org.aksw.jenax.arq.rdfterm;

import org.apache.jena.graph.Node;

public class RdfTermUtils {
    /** Return the rdf term type for the given node */
    public static RdfTermType classify(Node node) {
        RdfTermType result;
        if (node == null) {
            result = RdfTermType.UNKNOWN;
        } else if (node.isURI()) {
            result = RdfTermType.IRI;
        } else if (node.isLiteral()) {
            result = RdfTermType.LITERAL;
        } else if (node.isBlank()) {
            result = RdfTermType.BNODE;
        } else if (node.isNodeTriple()) {
            result = RdfTermType.TRIPLE;
        } else if (node.isVariable()) {
            result = RdfTermType.VARIABLE;
        } else {
            result = RdfTermType.UNKNOWN;
        }

        return result;
    }
}

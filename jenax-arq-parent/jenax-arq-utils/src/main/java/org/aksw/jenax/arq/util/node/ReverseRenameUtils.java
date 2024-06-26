package org.aksw.jenax.arq.util.node;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Rename;

/**
 * Utils to reverse var renames similar to {@link Rename} and more
 * specifically {@link Rename#reverseVarRename(org.apache.jena.sparql.algebra.Op, boolean)}.
 *
 * {@link Rename} does not provide an API to reverse-rename individual nodes and triples.
 *
 * @author raven
 *
 */
public class ReverseRenameUtils {

    /** Return the plain name of the given variable node. Raises an exception for non-variable nodes. */
    public static String plainVarName(Node node) {
        return plainVarName(node.getName());
    }

    public static String plainVarName(String rawName) {
        return separateMarkerFromVarName(rawName)[1];
    }

    public static String[] separateMarkerFromVarName(String rawName) {
        String[] result = new String[] {"", null};
        if(Var.isAllocVarName(rawName)) {
            result[0] = ARQConstants.allocVarMarker;
            result[1] = rawName.substring(ARQConstants.allocVarMarker.length());
        } else if(Var.isBlankNodeVarName(rawName)) {
            result[0] = ARQConstants.allocVarAnonMarker;
            result[1] = rawName.substring(ARQConstants.allocVarAnonMarker.length());
        } else if(Var.isRenamedVar(rawName)) {
            result[0] = ARQConstants.allocVarScopeHiding;
            result[1] = rawName.substring(ARQConstants.allocVarScopeHiding.length());
        } else {
            result[1] = rawName;
        }

        return result;
    }

    /** Return the node obtained by applying reverse-renaming to the argument  */
    public static Node effectiveNode(Node node) {
        Node result;
        if(Var.isVar(node)) {
            String rawName = node.getName();
            String name = separateMarkerFromVarName(rawName)[1];

            if(Var.isBlankNodeVar(node) || Var.isAllocVar(node)) {
                result = NodeFactory.createBlankNode(name);
            } else {
                result = node;
            }

        } else {
            result = node;
        }
        return result;
    }

    /** Return the node obtained by applying reverse-renaming to its nodes  */
    public static Triple effectiveTriple(Triple t) {
        return Triple.create(
                effectiveNode(t.getSubject()),
                effectiveNode(t.getPredicate()),
                effectiveNode(t.getObject()));
    }
}

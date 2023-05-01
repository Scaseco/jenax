package org.aksw.jena_sparql_api.sparql.ext.util;

import java.util.Collections;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.vocabulary.RDF;

public class PropFuncArgUtils {
    /**
     * If the argument is neither null nor rdf:nil then the result is a singleton
     * list containing it. Otherwise an empty list is returned.
     */
    public static List<Node> nodeToList(Node node) {
        List<Node> result = node == null || RDF.Nodes.nil.equals(node)
                ? Collections.emptyList()
                : Collections.singletonList(node);
        return result;
    }

    /** Return a list also if the given argument holds a single Node */
    public static List<Node> getAsList(PropFuncArg arg) {
        List<Node> result = arg.isNode() ? nodeToList(arg.getArg()) : arg.getArgList();
        return result;
    }
}

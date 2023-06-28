package org.aksw.jenax.arq.util.node;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class PathUtils {
    /** A pre-built path for the expression <code>rdf:type/rdfs:subclassOf*</code>. */
    public static final Path typeSubclassOf = PathFactory.pathSeq(
            PathFactory.pathLink(RDF.type.asNode()),
            PathFactory.pathZeroOrMore1(PathFactory.pathLink(RDFS.subClassOf.asNode())));

    public static P_Path0 createStep(String predicate, boolean isFwd) {
        return createStep(NodeFactory.createURI(predicate), isFwd);
    }

    public static P_Path0 createStep(Node predicate, boolean isFwd) {
        P_Path0 result = isFwd ? new P_Link(predicate) : new P_ReverseLink(predicate);
        return result;
    }

    public static Path create(Path path, boolean isFwd) {
        Path result = isFwd ? path : PathFactory.pathInverse(path);
        return result;
    }

    /** Return null unless path can be casted to P_Path0 */
    public static P_Path0 asStep(Path path) {
        return path instanceof P_Path0 ? (P_Path0)path : null;
    }


}

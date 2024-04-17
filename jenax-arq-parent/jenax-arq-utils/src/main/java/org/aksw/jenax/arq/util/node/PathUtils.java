package org.aksw.jenax.arq.util.node;

import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathCompiler;
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

    /**
     * Flatten the given triple path into a basic pattern if possible.
     * Returns null if the path cannot be flattened.
     */
    public static BasicPattern flattenOrNull(TriplePath triplePath) {
        PathCompiler pathCompiler = new PathCompiler();
        PathBlock pathBlock = pathCompiler.reduce(triplePath);
        BasicPattern result = canFlatten(pathBlock)
                ? flatten(pathBlock)
                : null;
        return result;
    }

    /**
     * Returns true iff all paths in the path block are instances of P_Path0, i.e.
     * merely forward or reverse links.
     */
    public static boolean canFlatten(PathBlock pathBlock) {
        boolean result = pathBlock.getList().stream()
                .allMatch(triplePath -> triplePath.getPath() instanceof P_Path0);
        return result;
    }

    /**
     * Flatten the path block w.r.t. {@link #canFlatten(PathBlock)}.
     * Raises a RuntimeException on failure.
     * <p />
     * The caller will most likely wish to rename the variables, such as using:
     * <pre>
     * {@code NodeTransformLib.transform(ReverseRenameUtils::effectiveNode, bgp);}
     * </pre>
     *
     * @param pathBlock
     * @return
     */
    public static BasicPattern flatten(PathBlock pathBlock) {
        BasicPattern bgp = new BasicPattern();
        for (TriplePath tp : pathBlock) {
            Path path = tp.getPath();
            if (path instanceof P_Path0) {
                P_Path0 p0 = (P_Path0)path;
                Triple t = TripleUtils.create(tp.getSubject(), p0.getNode(), tp.getObject(), p0.isForward());
                bgp.add(t);
            } else {
                throw new RuntimeException("Could not flatten path: " + path);
            }
        }
        return bgp;
    }
}

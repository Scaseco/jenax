package org.aksw.jenax.path.core;

import java.util.List;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathBase;
import org.aksw.commons.path.core.PathOps;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * Dedicated path implementation compatible with {@link Path}&lt;Node&gt;.
 *
 * @author raven
 *
 */
public class PathNode
    extends PathBase<Node, PathNode>
{
    private static final long serialVersionUID = 1L;

    public PathNode(PathOps<Node, PathNode> pathOps, boolean isAbsolute, List<Node> segments) {
        super(pathOps, isAbsolute, segments);
    }

    /** Convenience method for {@link Resource} */
    public PathNode resolve(Resource other) {
        return resolve(other.asNode());
    }

    /* Static convenience shorthands */

    public static PathNode parse(String str) {
        return PathOpsNode.get().fromString(str);
    }

    public static PathNode newAbsolutePath(Node segment) {
        return PathOpsNode.get().newAbsolutePath(segment);
    }

    public static PathNode newAbsolutePath(Node ... segments) {
        return PathOpsNode.get().newAbsolutePath(segments);
    }

    public static PathNode newAbsolutePath(List<Node> segments) {
        return PathOpsNode.get().newAbsolutePath(segments);
    }

    public static PathNode newRelativePath(Node segment) {
        return PathOpsNode.get().newRelativePath(segment);
    }

    public static PathNode newRelativePath(Node ... segments) {
        return PathOpsNode.get().newRelativePath(segments);
    }

    public static PathNode newRelativePath(List<Node> segments) {
        return PathOpsNode.get().newRelativePath(segments);
    }
}

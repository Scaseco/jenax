package org.aksw.jenax.path.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathOps;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.out.NodeFmtLib;


/**
 * Implementation of {@link PathOps} which allows for using the path machinery
 * with jena {@link Node}s.
 *
 * @author raven
 *
 */
public class PathOpsNode
    implements PathOps<Node, PathNode>, Serializable
{
    private static final long serialVersionUID = 1L;

    public static final Node PARENT = NodeFactory.createURI("..");
    public static final Node SELF = NodeFactory.createURI(".");

    private static PathOpsNode INSTANCE = null;

    private Object readResolve() {
        return get();
    }

    public static PathOpsNode get() {
        if (INSTANCE == null) {
            synchronized (PathOpsNode.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PathOpsNode();
                }
            }
        }
        return INSTANCE;
    }


    /** Convenience static shorthand for .get().newRoot() */
    public static PathNode newAbsolutePath() {
        return get().newRoot();
    }

    public static PathNode newRelativePath() {
        return get().newPath(false, Collections.emptyList());
    }

    @Override
    public PathNode upcast(Path<Node> path) {
        return (PathNode)path;
    }

    @Override
    public List<Node> getBasePathSegments() {
        return Collections.emptyList();
    }

    @Override
    public Comparator<Node> getComparator() {
        return NodeUtils::compareAlways;
    }

    @Override
    public PathNode newPath(boolean isAbsolute, List<Node> segments) {
        return new PathNode(this, isAbsolute, segments);
    }

    @Override
    public PathNode newPath(Node element) {
        return newPath(false, Collections.singletonList(element));
    }

    @Override
    public Node getSelfToken() {
        return SELF;
    }

    @Override
    public Node getParentToken() {
        return PARENT;
    }

    @Override
    public String toStringRaw(Object path) {
        return toString((PathNode)path);
    }

    @Override
    public String toString(PathNode path) {
        Node[] nodes = path.getSegments().stream()
                .collect(Collectors.toList()).toArray(new Node[0]);

        String result = (path.isAbsolute() ? "/" : "") + NodeFmtLib.strNodesNT(nodes);

        return result;
    }

    @Override
    public PathNode fromString(String str) {
        str = str.trim();

        boolean isAbsolute = false;

        if (str.startsWith("/")) {
            isAbsolute = true;
            str = str.substring(1);
        }

        // Code below adapted from NodeFactoryExtra.parseNode(str)

        List<Node> segments = NodeUtils.parseNodes(str, new ArrayList<>());

        return newPath(isAbsolute, segments);
    }

}

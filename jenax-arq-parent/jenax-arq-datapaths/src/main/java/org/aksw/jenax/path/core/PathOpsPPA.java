package org.aksw.jenax.path.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathOps;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.Iterators;

public class PathOpsPPA
    implements PathOps<AliasedStep, PathPPA>
{
    public static final AliasedStep PARENT = new AliasedStep(PathOpsNode.PARENT, true, null);
    public static final AliasedStep SELF = new AliasedStep(PathOpsNode.SELF, true, null);

    private static PathOpsPPA INSTANCE = null;

    public static PathOpsPPA get() {
        if (INSTANCE == null) {
            synchronized (PathOpsPPA.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PathOpsPPA();
                }
            }
        }
        return INSTANCE;
    }


    @Override
    public PathPPA upcast(Path<AliasedStep> path) {
        return (PathPPA)path;
    }

    @Override
    public List<AliasedStep> getBasePathSegments() {
        return Collections.emptyList();
    }

    @Override
    public Comparator<AliasedStep> getComparator() {
        return Comparator.comparing(Object::toString);
    }

    @Override
    public PathPPA newPath(boolean isAbsolute, List<AliasedStep> segments) {
        return new PathPPA(this, isAbsolute, segments);
    }

    @Override
    public PathPPA newPath(AliasedStep element) {
        return newPath(false, Collections.singletonList(element));
    }

    @Override
    public AliasedStep getSelfToken() {
        return SELF;
    }

    @Override
    public AliasedStep getParentToken() {
        return PARENT;
    }

    @Override
    public String toStringRaw(Object path) {
        return toString((PathPPA)path);
    }

    /** Note the current serialization depends on nodes to be IRIs! */

    /** Convert an aliased step to a sequence of nodes. The nodes are used for serialization. */
    public static List<Node> toNodes(AliasedStep step) {
        List<Node> result = new ArrayList<>(3);
        result.add(step.getNode());

        if(!step.isForward()) {
            result.add(NodeValue.FALSE.asNode());
        }

        if (step.getAlias() != null) {
            result.add(NodeFactory.createURI(step.getAlias()));
        }

        return result;
    }

    @Override
    public String toString(PathPPA path) {
        List<AliasedStep> segments = path.getSegments();

        Node[] nodes = segments.stream()
                .map(PathOpsPPA::toNodes)
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                .toArray(new Node[0]);

        String result = (path.isAbsolute() ? "/" : "") + NodeFmtLib.strNodesNT(nodes);
        return result;
    }

    /**
     * Serialization is an optional leading '/' for the root, following by a sequence of RDF terms:
     * path := '/'? (predicateIRI isForward? alias?)*
     * isFoward := boolean # true | false
     * alias := String
     * isForward defaults to true if omitted.
     *
     */
    @Override
    public PathPPA fromString(String str) {
        str = str.trim();
        boolean isAbsolute = false;

        if (str.startsWith("/")) {
            isAbsolute = true;
            str = str.substring(1);
        }

        // Code below adapted from NodeFactoryExtra.parseNode(str)

        List<Node> nodes = NodeUtils.parseNodes(str, new ArrayList<>());

        List<AliasedStep> steps = new ArrayList<>();
        Iterator<Node> it = nodes.iterator();
        Node current = Iterators.getNext(it, null);
        while (current != null) {
            Node p;
            boolean isFwd = true;
            String alias = null;

            if (current.isURI()) {
                p = current;
                current = Iterators.getNext(it, null);
            } else {
                throw new RuntimeException("Expected an IRI, got: " + current);
            }

            if (current != null && current.isLiteral()) {
                NodeValue nv = NodeValue.makeNode(current);
                if (nv.isBoolean()) {
                    isFwd = nv.getBoolean();
                    current = Iterators.getNext(it, null);
                }
            }

            if (current != null && current.isLiteral()) {
                NodeValue nv = NodeValue.makeNode(current);
                if (nv.isString()) {
                    alias = nv.getString();
                    current = Iterators.getNext(it, null);
                }
            }

            AliasedStep step = new AliasedStep(p, isFwd, alias);
            steps.add(step);
        }

        return newPath(isAbsolute, steps);
    }
}

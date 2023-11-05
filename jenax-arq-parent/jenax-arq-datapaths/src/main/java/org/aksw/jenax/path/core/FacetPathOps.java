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
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.Iterators;

public class FacetPathOps
    implements PathOps<FacetStep, FacetPath>
{
    public static final FacetStep PARENT = new FacetStep(PathOpsNode.PARENT, true, null);
    public static final FacetStep SELF = new FacetStep(PathOpsNode.SELF, true, null);

    private static FacetPathOps INSTANCE = null;

    public static FacetPathOps get() {
        if (INSTANCE == null) {
            synchronized (FacetPathOps.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FacetPathOps();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public FacetPath upcast(Path<FacetStep> path) {
        return (FacetPath)path;
    }

    @Override
    public List<FacetStep> getBasePathSegments() {
        return Collections.emptyList();
    }

    @Override
    public Comparator<FacetStep> getComparator() {
        return Comparator.comparing(Object::toString);
    }

    @Override
    public FacetPath newPath(boolean isAbsolute, List<FacetStep> segments) {
        return new FacetPath(this, isAbsolute, segments);
    }

    @Override
    public FacetStep getSelfToken() {
        return SELF;
    }

    @Override
    public FacetStep getParentToken() {
        return PARENT;
    }

    @Override
    public String toStringRaw(Object path) {
        return toString((FacetPath)path);
    }

    /** Note the current serialization depends on nodes to be IRIs! */

    /** Convert an aliased step to a sequence of nodes. The nodes are used for serialization. */
    public static List<Node> toNodes(FacetStep step) {
        List<Node> result = new ArrayList<>(4);
        result.add(step.getNode());

        if(!step.isForward()) {
            result.add(NodeValue.FALSE.asNode());
        }

        String alias = step.getAlias();
        if (alias != null && !alias.isEmpty()) {
            result.add(NodeFactory.createLiteral(alias));
        }

        Node targetComponent = step.getTargetComponent();
        if (!FacetStep.isTarget(targetComponent)) {
            result.add(targetComponent);
        }

        return result;
    }

    @Override
    public String toString(FacetPath path) {
        List<FacetStep> segments = path.getSegments();

        Node[] nodes = segments.stream()
                .map(FacetPathOps::toNodes)
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                .toArray(new Node[0]);

        String result = (path.isAbsolute() ? "/" : "") + NodeFmtLib.strNodesTTL(nodes);
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
    public FacetPath fromString(String str) {
        str = str.trim();
        boolean isAbsolute = false;

        if (str.startsWith("/")) {
            isAbsolute = true;
            str = str.substring(1);
        }

        // Code below adapted from NodeFactoryExtra.parseNode(str)

        List<Node> nodes = NodeUtils.parseNodes(str, new ArrayList<>());

        List<FacetStep> steps = new ArrayList<>();
        Iterator<Node> it = nodes.iterator();
        Node current = Iterators.getNext(it, null);
        while (current != null) {
            Node p;
            boolean isFwd = true;
            String alias = null;
            Var targetComponent = FacetStep.TARGET;

            if (current.isURI()) {
                p = current;
                current = Iterators.getNext(it, null);
            } else {
                throw new RuntimeException("Property URI expected. got: " + current);
            }

//            if (!current.isLiteral()) {
//                p = current;
//            } else {
//                throw new RuntimeException("Unexpected literal in path (this serialization does not support literals for transitions). got: " + current);
//            }

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

            if (current != null && current.isLiteral()) {
                if (current.isVariable()) {
                    targetComponent = (Var)current;
                    // alias = nv.getString();
                    current = Iterators.getNext(it, null);
                }
            }

            FacetStep step = new FacetStep(p, isFwd, alias, targetComponent);
            steps.add(step);
        }

        return newPath(isAbsolute, steps);
    }
}

package org.aksw.facete.v3.api;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.node.NodeCustom;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.HasFacetPath;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;

public class NodeFacetPath
    extends NodeCustom<HasFacetPath> // Does not extend from HasFacetPath itself because usage of Node and HasFacetPath should reside in different architecture layers
{
    protected NodeFacetPath(HasFacetPath value) {
        super(value);
    }

    public static NodeFacetPath of(HasFacetPath path) {
        return new NodeFacetPath(path);
    }


    /**
     * Extract all TreeQueryNode references from the given expression
     */
    public static Set<TreeQueryNode> mentionedPathNodes(Expr expr) {
        Set<TreeQueryNode> result = ExprUtils.nodesMentioned(expr).stream()
                .flatMap(x -> ObjectUtils.tryCastAs(NodeFacetPath.class, x).stream())
                .map(NodeFacetPath::getValue)
                .map(x -> (TreeQueryNode)x)
                .collect(Collectors.toCollection(HashSet::new));
        return result;
    }

    public static Set<FacetPath> mentionedPaths(Expr expr) {
        Set<FacetPath> result = ExprUtils.nodesMentioned(expr).stream()
                .flatMap(x -> ObjectUtils.tryCastAs(NodeFacetPath.class, x).stream())
                .map(NodeFacetPath::getValue)
                .map(HasFacetPath::getFacetPath)
                .collect(Collectors.toCollection(HashSet::new));
        return result;
    }

    public static Expr asExpr(FacetPath facetPath) {
        Node node = of(() -> facetPath);
        return ExprLib.nodeToExpr(node);
    }
}

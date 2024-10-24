package org.aksw.facete.v3.api;

import org.aksw.jenax.arq.util.node.NodeCustom;
import org.aksw.jenax.path.core.HasFacetPath;

@Deprecated // Use NodeCustom directly
public class NodeFacetPath
    extends NodeCustom<HasFacetPath> // Does not extend from HasFacetPath itself because usage of Node and HasFacetPath should reside in different architecture layers
{
    protected NodeFacetPath(HasFacetPath value) {
        super(value);
    }

    public static NodeFacetPath of(HasFacetPath path) {
        return new NodeFacetPath(path);
    }
//
//    /** Substitute all referenced paths in an expression w.r.t. the given path mapping */
//    public static Expr resolveExpr(FacetPathMapping pathMapping, Expr expr) {
//        NodeTransform nodeTransform = createNodeTransform(pathMapping);
//        Expr result = ExprTransformer.transform(new NodeTransformExpr(nodeTransform), expr);
//        return result;
//    }
//
//    /** Create a NodeTransform for substituting NodeFacetPath instances with variables */
//    public static NodeTransform createNodeTransform(FacetPathMapping mapping) {
//        return NodeTransformLib2.wrapWithNullAsIdentity(x -> x instanceof NodeFacetPath
//            ? mapping.allocate(((NodeFacetPath)x).getValue().getFacetPath())
//            : null);
//    }
//
//    /**
//     * Extract all TreeQueryNode references from the given expression
//     */
//    public static Set<TreeQueryNode> mentionedPathNodes(Expr expr) {
//        Set<TreeQueryNode> result = ExprUtils.nodesMentioned(expr).stream()
//                .flatMap(x -> ObjectUtils.tryCastAs(NodeFacetPath.class, x).stream())
//                .map(NodeFacetPath::getValue)
//                .map(x -> (TreeQueryNode)x)
//                .collect(Collectors.toCollection(HashSet::new));
//        return result;
//    }
//
//    public static Set<FacetPath> mentionedPaths(Expr expr) {
//        Set<FacetPath> result = ExprUtils.nodesMentioned(expr).stream()
//                .flatMap(x -> ObjectUtils.tryCastAs(NodeFacetPath.class, x).stream())
//                .map(NodeFacetPath::getValue)
//                .map(HasFacetPath::getFacetPath)
//                .collect(Collectors.toCollection(HashSet::new));
//        return result;
//    }
//
//    public static Expr asExpr(FacetPath facetPath) {
//        Node node = of(() -> facetPath);
//        return ExprLib.nodeToExpr(node);
//    }
}

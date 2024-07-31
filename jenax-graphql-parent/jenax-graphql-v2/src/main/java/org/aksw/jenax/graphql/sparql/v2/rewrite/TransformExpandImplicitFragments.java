package org.aksw.jenax.graphql.sparql.v2.rewrite;

/**
 * Expands fields annotated with {@code skip(if: "sparqlExpr")} and @code{include(if: "sparqlExpr)}
 * to inline fragments for uniform processing.
 *
 */
//public class TransformExpandImplicitFragments
//	extends NodeVisitorStub
//{
//	@Override
//	public TraversalControl visitField(Field node, TraverserContext<Node> context) {
//		if (!context.isVisited()) {
//			GraphQlUtils.expectAtMostOneDirective(node, "skip");
//
//
//		}
//	}
//
//}

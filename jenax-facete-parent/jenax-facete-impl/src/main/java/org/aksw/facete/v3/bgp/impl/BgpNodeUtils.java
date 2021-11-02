package org.aksw.facete.v3.bgp.impl;

import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.utils.PathAccessorImpl;
import org.aksw.jena_sparql_api.data_query.impl.FacetedQueryGenerator;
import org.aksw.jenax.sparql.path.SimplePath;

public class BgpNodeUtils {
    public static SimplePath toSimplePath(BgpNode fn) {
        SimplePath result = FacetedQueryGenerator.toSimplePath(fn, new PathAccessorImpl(fn.getModel()));
        return result;
//		BgpNode o;
//		o.parent().
//		List<P_Path0> steps =
//			Streams.stream(
//					Traverser.<BgpNode>forTree(x ->
//			Optional.ofNullable(x.parent())
//				.map(BgpMultiNode::parent)
//				.map(Collections::singleton)
//				.orElse(Collections.emptySet()))
//			.depthFirstPreOrder(fn))
//			.filter(x -> x.parent() != null)
//			.map(x -> PathUtils.createStep(x.parent().reachingProperty().asNode(), x.parent().getDirection().isForward()))
//			.collect(Collectors.toList());
//
//		SimplePath result = new SimplePath(steps);
//		return result;
    }

}

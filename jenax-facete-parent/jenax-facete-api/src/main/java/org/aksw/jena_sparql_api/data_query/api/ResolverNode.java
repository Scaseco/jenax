package org.aksw.jena_sparql_api.data_query.api;

import java.util.Collection;

import org.aksw.facete.v3.api.traversal.TraversalNode;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;

public interface ResolverNode
    extends TraversalNode<ResolverNode, ResolverDirNode, ResolverMultiNode>
{
    Collection<BinaryRelation> getPaths();
}
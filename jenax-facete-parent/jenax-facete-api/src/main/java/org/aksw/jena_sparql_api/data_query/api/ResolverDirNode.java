package org.aksw.jena_sparql_api.data_query.api;

import java.util.Collection;

import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.traversal.TraversalDirNode;
import org.aksw.jenax.sparql.relation.api.TernaryRelation;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public interface ResolverDirNode
    extends TraversalDirNode<ResolverNode, ResolverMultiNode>
{
    Collection<TernaryRelation> getContrib();
    Query rewrite(Query query);

    SparqlQueryConnection virtualConn();
    FacetedQuery toFacetedQuery();
}

package org.aksw.jena_sparql_api.data_query.api;

import org.aksw.jenax.sparql.relation.api.BinaryRelation;;

public interface PathAccessorRdf<P>
    extends PathAccessorSimple<P>
{
    BinaryRelation getReachingRelation(P path);

    boolean isReverse(P path);
    String getPredicate(P path);

    String getAlias(P path);
}

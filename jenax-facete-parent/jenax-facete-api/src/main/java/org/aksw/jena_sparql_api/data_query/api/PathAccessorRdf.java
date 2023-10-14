package org.aksw.jena_sparql_api.data_query.api;

import org.aksw.jenax.sparql.fragment.api.Fragment2;;

public interface PathAccessorRdf<P>
    extends PathAccessorSimple<P>
{
    Fragment2 getReachingRelation(P path);

    boolean isReverse(P path);
    String getPredicate(P path);

    String getAlias(P path);
}

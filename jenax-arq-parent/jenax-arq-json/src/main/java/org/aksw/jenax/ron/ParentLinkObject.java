package org.aksw.jenax.ron;

import org.apache.jena.sparql.path.P_Path0;

public interface ParentLinkObject
    extends ParentLink
{
    @Override
    RdfObject getParent();

    P_Path0 getKey();
}

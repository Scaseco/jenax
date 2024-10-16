package org.aksw.jenax.graphql.sparql.v2.ron;

public interface ParentLinkArray
    extends ParentLink
{
    @Override
    RdfArray getParent();

    int getIndex();
}

package org.aksw.jenax.ron;

public interface ParentLinkArray
    extends ParentLink
{
    @Override
    RdfArray getParent();

    int getIndex();
}

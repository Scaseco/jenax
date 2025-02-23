package org.aksw.jenax.dataaccess.sparql.linksource;

import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;

public interface RDFLinkSourceWrapper<X extends RDFLinkSource>
    extends RDFLinkSource
{
    X getDelegate();

    @Override
    default RDFLinkBuilder<?> newLinkBuilder() {
        X tmp = getDelegate();
        return tmp.newLinkBuilder();
    }
}

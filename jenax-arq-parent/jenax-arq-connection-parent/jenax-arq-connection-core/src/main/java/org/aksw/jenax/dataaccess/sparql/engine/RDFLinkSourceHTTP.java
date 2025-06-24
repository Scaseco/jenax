package org.aksw.jenax.dataaccess.sparql.engine;

import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderHTTP;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;

public interface RDFLinkSourceHTTP
    extends RDFLinkSource
{
    @Override
    public RDFLinkBuilderHTTP<?> newLinkBuilder();
}

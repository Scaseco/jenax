package org.aksw.jenax.dataaccess.sparql.engine;

import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderHTTP;

public interface RDFEngineOverHTTP
    extends RDFEngine
{
    @Override
    public RDFLinkBuilderHTTP newLinkBuilder();
}

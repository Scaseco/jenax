package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;

// Experimental - not fond of having closeable and non-closeable versions of a link source
public interface CloseableRDFLinkSource
    extends RDFLinkSource, AutoCloseable
{
}

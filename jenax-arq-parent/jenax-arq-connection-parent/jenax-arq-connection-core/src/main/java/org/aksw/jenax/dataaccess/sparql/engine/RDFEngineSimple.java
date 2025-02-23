package org.aksw.jenax.dataaccess.sparql.engine;

import java.io.Closeable;
import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;

public class RDFEngineSimple
    implements RDFEngine
{
    protected RDFLinkSource linkSource;
    protected AutoCloseable closeAction;

    /** Constructed with {@link RDFEngines#of(RDFLinkSource, Closeable)}. */
    protected RDFEngineSimple(RDFLinkSource linkSource, AutoCloseable closeAction) {
        super();
        this.linkSource = Objects.requireNonNull(linkSource);
        this.closeAction = closeAction;
    }

    @Override
    public RDFLinkSource getLinkSource() {
        return linkSource;
    }

    @Override
    public void close() throws Exception {
        if (closeAction != null) {
            closeAction.close();
        }
    }
}

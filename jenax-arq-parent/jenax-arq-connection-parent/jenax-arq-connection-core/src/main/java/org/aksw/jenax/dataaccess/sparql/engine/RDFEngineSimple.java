package org.aksw.jenax.dataaccess.sparql.engine;

import java.io.Closeable;
import java.util.Objects;
import java.util.Optional;

import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;

public class RDFEngineSimple
    implements RDFEngine
{
    protected RDFLinkSource linkSource;
    protected ServiceControl service;
    protected AutoCloseable closeAction;

    protected RDFEngineSimple(RDFLinkSource linkSource, AutoCloseable closeAction) {
        this(linkSource, null, closeAction);
    }

    /** Constructed with {@link RDFEngines#of(RDFLinkSource, Closeable)}. */
    protected RDFEngineSimple(RDFLinkSource linkSource, ServiceControl service, AutoCloseable closeAction) {
        super();
        this.linkSource = Objects.requireNonNull(linkSource);
        this.service = service;
        this.closeAction = closeAction;
    }

    @Override
    public RDFLinkSource getLinkSource() {
        return linkSource;
    }

    @Override
    public Optional<ServiceControl> getServiceControl() {
        return Optional.ofNullable(service);
    }

    @Override
    public void close() throws Exception {
        if (closeAction != null) {
            closeAction.close();
        }
    }
}

package org.aksw.jenax.dataaccess.sparql.pod;

import java.io.Closeable;
import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngines;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;

public class RDFDataPodSimple
    implements RDFDataPod
{
    protected RDFDataSource dataSource;
    protected AutoCloseable closeAction;

    /** Constructed with {@link RDFEngines#of(RDFLinkSource, Closeable)}. */
    protected RDFDataPodSimple(RDFDataSource dataSource, AutoCloseable closeAction) {
        super();
        this.dataSource = Objects.requireNonNull(dataSource);
        this.closeAction = closeAction;
    }

    @Override
    public RDFDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void close() throws Exception {
        if (closeAction != null) {
            closeAction.close();
        }
    }
}

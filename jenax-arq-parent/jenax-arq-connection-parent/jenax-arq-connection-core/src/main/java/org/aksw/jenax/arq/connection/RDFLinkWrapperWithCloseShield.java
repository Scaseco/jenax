package org.aksw.jenax.arq.connection;

import org.apache.jena.rdflink.RDFLink;

public class RDFLinkWrapperWithCloseShield
    extends RDFLinkWrapper
{
    public RDFLinkWrapperWithCloseShield(RDFLink delegate) {
        super(delegate);
    }

    @Override
    public void close() {
        // no op
    }

// We could add a local 'isClosed' flag but then the builders for "newQuery" and "newUpdate" need to be wrapped to check that flag
//    @Override
//    public boolean isClosed() {
//    }
}

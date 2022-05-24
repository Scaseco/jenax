package org.aksw.jena_sparql_api.cache.staging;

import java.io.Closeable;
import java.io.InputStream;

import org.aksw.commons.util.closeable.AutoCloseables;
import org.aksw.jena_sparql_api.cache.extra.InputStreamProvider;


public class InputStreamProviderInputStreamClosable
    implements InputStreamProvider
{
    private Closeable closeable;
    private InputStream in;

    public InputStreamProviderInputStreamClosable(InputStream in, Closeable closeable) {
        this.in = in;
        this.closeable = closeable;
    }

    @Override
    public InputStream open() {
        return in;
    }

    @Override
    public void close() {
    	AutoCloseables.close(closeable);
    }
}

package org.aksw.jena_sparql_api.cache.staging;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamClosable
    extends InputStreamDecorator
{
    private Closeable closable;

    public InputStreamClosable(InputStream decoratee, Closeable closable) {
        super(decoratee);
        this.closable = closable;
    }

    @Override
    public void close() throws IOException {
        closable.close();
        super.close();
    }
}
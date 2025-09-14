package org.aksw.jenax.dataaccess.sparql.creator.source;

import java.io.IOException;
import java.io.InputStream;

public interface Source {
    String getName();
    InputStream open() throws IOException;
}

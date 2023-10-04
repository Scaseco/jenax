package org.aksw.jenax.graphql;

import java.io.IOException;
import java.io.OutputStream;

public interface GraphQlResponseWriter {
    public void write(OutputStream out, GraphQlExec exec) throws IOException;
}

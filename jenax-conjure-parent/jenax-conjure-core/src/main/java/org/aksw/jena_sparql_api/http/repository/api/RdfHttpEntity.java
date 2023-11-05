package org.aksw.jena_sparql_api.http.repository.api;

import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.rdf.model.Resource;

public interface RdfHttpEntity {
    // RdfHttpResourceFile getHttpResource();

    Resource getCombinedInfo();

    InputStream open() throws IOException;


    // CompletableFuture<?> write(Consumer<OutputStream> writer);
//    WriteProgress put(InputStream in);
}

package org.aksw.jenax.fuseki.mod.graphql;

import java.io.IOException;

import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.system.stream.StreamManager;

public class ResourceSourceFromStreamManager
    extends ResourceSourceBase
{
    protected String resourceName;

    public ResourceSourceFromStreamManager(String contentType, String resourceName) {
        super(contentType);
        this.resourceName = resourceName;
    }

    @Override
    public TypedInputStream open() throws IOException {
        StreamManager streamMgr = StreamManager.get();
        TypedInputStream base = streamMgr.open(resourceName);

        // Override the content type if set here
        TypedInputStream result = contentType == null
            ? base
            // Note: Base's input stream is not unwrapped in order to not leave base open.
            : new TypedInputStream(base, this.contentType);

        return result;
    }
}

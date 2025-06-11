package org.aksw.jenax.fuseki.mod.graphql;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.jena.atlas.web.TypedInputStream;

public class ResourceSourceStatic
    extends ResourceSourceBase
{
    private byte[] payload;

    public ResourceSourceStatic(String contentType, byte[] payload) {
        super(contentType);
        this.payload = payload;
    }

    @Override
    public TypedInputStream open() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(payload);
        return new TypedInputStream(bais, contentType);
    }
}

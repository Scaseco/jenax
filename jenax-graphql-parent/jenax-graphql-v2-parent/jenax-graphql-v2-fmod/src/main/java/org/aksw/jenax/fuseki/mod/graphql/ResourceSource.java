package org.aksw.jenax.fuseki.mod.graphql;

import java.io.IOException;

import org.apache.jena.atlas.web.TypedInputStream;

public interface ResourceSource {
    TypedInputStream open() throws IOException;
}

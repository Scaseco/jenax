package org.aksw.jenax.engine.docker.common;

import org.aksw.jenax.model.osreo.ImageIntrospection;

/** Introspects images by launching containers. */
public interface ImageIntrospector {
    ImageIntrospection inspect(String image);
}

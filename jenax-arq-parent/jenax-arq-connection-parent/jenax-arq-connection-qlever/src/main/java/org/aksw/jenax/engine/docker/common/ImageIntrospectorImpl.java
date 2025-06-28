package org.aksw.jenax.engine.docker.common;

import org.aksw.jenax.model.osreo.ImageIntrospection;
import org.apache.jena.rdf.model.Model;

public class ImageIntrospectorImpl
    implements ImageIntrospector
{
    protected Model model;

    @Override
    public ImageIntrospection inspect(String image) {
        // Check whether the image exists.

        // Try without entry point (perhaps don't do this?)

        // Try shells as entry points

        // Try locators (unless the shell has a locator)
        // XXX If the shell's declared locator fails then perhaps try another shell?

        return null;
    }
}

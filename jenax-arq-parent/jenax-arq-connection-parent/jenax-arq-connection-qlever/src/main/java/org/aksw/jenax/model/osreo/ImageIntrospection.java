package org.aksw.jenax.model.osreo;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Base;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.apache.jena.rdf.model.Resource;

@Base("https://w3id.org/osreo#")
public interface ImageIntrospection
    extends Resource
{
    @Iri // ("shellSupport")
    Set<ShellSupport> getShellSupport();

    // "Which" resolutions.
    // Map<String, String> getWhichMap();
}

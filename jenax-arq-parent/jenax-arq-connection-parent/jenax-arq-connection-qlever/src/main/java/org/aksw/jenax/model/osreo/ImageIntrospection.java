package org.aksw.jenax.model.osreo;

import java.util.Map;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.KeyIri;
import org.aksw.jenax.annotation.reprogen.ValueIri;
import org.apache.jena.rdf.model.Resource;

public interface ImageIntrospection
    extends Resource
{
    // @Iri // ("shellSupport")
    // Set<ShellSupport> getShellSupport();
    @Iri(OsreoTerms.O + "shellSupport")
    @KeyIri("urn:key")
    @ValueIri("urn:value")
    Map<String, ShellSupport> getShellStatus();

//    ShellSupport getOrAddShellSupport(String shellName) {
//
//    }

    // "Which" resolutions.
    // Map<String, String> getWhichMap();
}

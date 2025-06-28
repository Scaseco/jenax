package org.aksw.jenax.model.osreo;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.apache.jena.rdf.model.Resource;

// @Namespace(OsreoTerms.O)
public interface HasProbeLocation
    extends Resource
{
    @Iri(OsreoTerms.probeLocation)
    Set<String> getProbeLocations();
}

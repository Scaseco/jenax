package org.aksw.jenax.model.osreo;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Base;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.apache.jena.rdf.model.Resource;

@Base("https://w3id.org/osreo#")
public interface HasProbeLocation
    extends Resource
{
    @Iri("propeLocation")
    Set<String> getPropeLocations();
}

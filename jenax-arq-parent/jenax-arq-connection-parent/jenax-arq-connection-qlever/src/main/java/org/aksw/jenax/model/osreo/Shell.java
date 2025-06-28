package org.aksw.jenax.model.osreo;

import org.aksw.jenax.annotation.reprogen.Base;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.model.rdfs.domain.api.HasRdfsLabel;

@Base("https://w3id.org/osreo#")
public interface Shell
    extends HasRdfsLabel, HasProbeLocation
{
    /**
     * Some shells have a built-in command locator.
     * For example, bash has built-in support for "which".
     */
    @Iri
    String getLocatorCommand();
    Shell setLocatorCommand(String locatorCommand);
}

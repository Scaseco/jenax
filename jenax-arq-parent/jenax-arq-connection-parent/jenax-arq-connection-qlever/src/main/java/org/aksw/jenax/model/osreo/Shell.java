package org.aksw.jenax.model.osreo;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.Namespace;
import org.aksw.jenax.model.rdfs.domain.api.HasRdfsLabel;

@Namespace(OsreoTerms.O)
public interface Shell
    extends HasRdfsLabel, HasProbeLocation, HasCommandPrefix
{
    /**
     * Some shells have a built-in command locator.
     * For example, bash has built-in support for "which".
     */
    @Iri
    String getLocatorCommand();
    Shell setLocatorCommand(String locatorCommand);
}

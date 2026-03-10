package org.aksw.jenax.model.osreo;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.apache.jena.rdf.model.Resource;

/**
 * Arguments to be appended before a user command.
 * E.g. to use the bash shell with a user command, the "-c" option is needed.
 *
 */
public interface HasCommandOption
    extends Resource
{
    @Iri(OsreoTerms.commandOption)
    String getCommandOption();
    HasCommandOption setCommandOption(String commandOption);
}

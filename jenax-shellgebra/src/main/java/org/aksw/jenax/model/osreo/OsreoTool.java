package org.aksw.jenax.model.osreo;

import org.aksw.jenax.annotation.reprogen.Iri;

public interface OsreoTool {
//    @Iri(OsreoTerms.imageName)
//    String getImageName();

    /** Optional preferred tag of an image where the tool is guaranteed to work.
     *  If not set, 'latest' will be used. */
//    @Iri(OsreoTerms.preferredImageTag)
//    String getPreferredImageTag();

//    @Iri(OsreoTerms.commandName)
//    String getCommandName();

    @Iri(OsreoTerms.commandPath)
    String getCommandPath();
}

package org.aksw.jenax.model.osreo;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.Namespace;
import org.apache.jena.rdf.model.Resource;

@Namespace(OsreoTerms.O)
public interface ShellSupport
    extends HasCommandPrefix
{
    // Shell support must be referenced by a single ImageIntrospection
    @HashId
    @Iri(OsreoTerms.shellSupport)
    @Inverse
    ImageIntrospection getOwner();

    @Iri(OsreoTerms.shellType)
    Resource getShellType();
    ShellSupport setShellType(Resource shellType);

    @HashId
    @Iri(OsreoTerms.commandPath)
    String getCommandPath();
    ShellSupport setCommandPath(String commandPath);

    /** With this shell in the given container, use this locator command. */
    @Iri(OsreoTerms.locatorCommand)
    String getLocatorCommand();
    ShellSupport setLocatorCommand(String locatorCommand);
}

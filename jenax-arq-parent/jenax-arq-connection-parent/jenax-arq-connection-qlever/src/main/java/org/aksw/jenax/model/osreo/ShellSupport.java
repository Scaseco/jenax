package org.aksw.jenax.model.osreo;

import org.aksw.jenax.annotation.reprogen.Base;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.apache.jena.rdf.model.Resource;

@Base("https://w3id.org/osreo#")
public interface ShellSupport
    extends Resource
{
    // Shell support must be referenced by a single ImageIntrospection
    @HashId
    @Iri("shellSupport")
    @Inverse
    ImageIntrospection getOwner();

    Resource getShellType();
    ShellSupport setShellType(Resource shellType);

    @HashId
    String getCommandPath();
    ShellSupport setCommandPath(String commandPath);

    /** With this shell in the given container, use this locator command. */
    @Iri
    String getLocatorCommand();
    ShellSupport setLocatorCommand(String locatorCommand);
}

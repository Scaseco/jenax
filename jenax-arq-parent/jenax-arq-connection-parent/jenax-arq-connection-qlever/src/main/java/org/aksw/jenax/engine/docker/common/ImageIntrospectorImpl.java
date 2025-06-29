package org.aksw.jenax.engine.docker.common;

import java.util.List;
import java.util.Set;

import org.aksw.jenax.model.osreo.ImageIntrospection;
import org.aksw.jenax.model.osreo.LocatorCommand;
import org.aksw.jenax.model.osreo.OsreoUtils;
import org.aksw.jenax.model.osreo.Shell;
import org.aksw.jenax.model.osreo.ShellSupport;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class ImageIntrospectorImpl
    implements ImageIntrospector
{
    protected Model model;

    protected List<Shell> shells;
    protected List<LocatorCommand> locatorCommands;

    public static ImageIntrospector of(Model osreoModel) {
        List<Shell> shells = OsreoUtils.listShells(osreoModel);
        List<LocatorCommand> locatorCommands = OsreoUtils.listLocatorCommands(osreoModel);
        return new ImageIntrospectorImpl(shells, locatorCommands);
    }

    public ImageIntrospectorImpl(List<Shell> shells, List<LocatorCommand> locatorCommands) {
        super();
        this.shells = shells;
        this.locatorCommands = locatorCommands;
    }

    @Override
    public ImageIntrospection inspect(String image, boolean pullIfAbsent) {
        ImageIntrospection result = ModelFactory.createDefaultModel()
            .createResource().as(ImageIntrospection.class);

        if (ImageUtils.imageExists(image, pullIfAbsent)) {
            findShell(result, image);
        }

        // Try without entry point (perhaps don't do this?)

        return result;
    }

    protected void findShell(ImageIntrospection result, String imageName) {
        outer: for (Shell shell : shells) {
            String shellName = shell.getLabel();
            String shellPrefix = shell.getCommandPrefix();
            Set<String> shellProbeLocations = shell.getProbeLocations();
            for (String shellLocation : shellProbeLocations) {
                if (ContainerUtils.canRunEntrypoint(imageName, shellLocation, shellPrefix)) {
                    ShellSupport sh = result.getShellStatus().computeIfAbsent(shellName, key -> {
                        return result.getModel().createResource().as(ShellSupport.class);
                    });
                    sh.setCommandPath(shellLocation);
                    sh.setShellType(shell);
                    sh.setCommandPrefix(shell.getCommandPrefix());

                    // Check for the locator command
                    String builtInLocator = shell.getLocatorCommand();
                    if (builtInLocator != null) {
                        sh.setLocatorCommand(builtInLocator); // TODO Verify?
                    } else {
                        for (LocatorCommand locatorCommand : locatorCommands) {
                            for (String locatorLocation : locatorCommand.getProbeLocations()) {
                                if (ContainerUtils.hasCommand(imageName, shellLocation, shellPrefix, locatorLocation)) {
                                    sh.setLocatorCommand(locatorLocation);
                                }
                            }
                        }
                    }
                    break outer;
                }
            }
        }
    }
}

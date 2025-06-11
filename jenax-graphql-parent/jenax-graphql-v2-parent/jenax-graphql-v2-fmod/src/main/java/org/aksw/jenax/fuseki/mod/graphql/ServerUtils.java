package org.aksw.jenax.fuseki.mod.graphql;

import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerUtils {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static String getExternalForm(Class<?> clazz) {
        ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        String externalForm = location.toExternalForm();

        if (logger.isDebugEnabled()) {
            logger.debug("Trying to resolve webapp by starting from location (external form): " + externalForm);
        }

        Path path;
        try {
            // TODO This assumes that builds are done under /target/classes/
            path = Paths.get(location.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // Try to detect whether we are being run from an
        // archive (uber jar / war) or just from compiled classes
        if (externalForm.endsWith("/classes/")) {
            Path webappFolder = path.resolve("../../src/main/webapp").normalize();
            if(Files.exists(webappFolder)) {
                externalForm = webappFolder.toString();
            }
        } else if(externalForm.endsWith("-classes.jar")) {
            Path parent = path.getParent();
            String rawFilename = "" + path.getFileName();
            String filename = rawFilename.replace("-classes.jar", ".war");
            // Try if replacing '-classes.jar' with '.war' also exists
            Path warPath = parent.resolve(filename);
            if(Files.exists(warPath)) {
                externalForm = warPath.toString();
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Resolved webapp location to: " + externalForm);
        }

        return externalForm;
    }
}

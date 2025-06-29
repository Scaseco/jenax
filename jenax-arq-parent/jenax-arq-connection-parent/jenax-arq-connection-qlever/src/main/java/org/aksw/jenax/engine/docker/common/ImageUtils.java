package org.aksw.jenax.engine.docker.common;

import org.testcontainers.DockerClientFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.NotFoundException;

/**
 * Utils to inspect images and whether they exist.
 * Note: Methods that start containers for probing are in {@link ContainerUtils}.
 *
 */
public class ImageUtils {
    public static boolean imageExists(String imageName, boolean pullIfAbsent) {
        DockerClient client = DockerClientFactory.instance().client();
        boolean didPull = false;

        boolean result = false;
        while (true) {
            try {
                client.inspectImageCmd(imageName).exec();
                result = true;
            } catch (NotFoundException e) {
                if (!didPull && pullIfAbsent && pullImage(imageName)) {
                    didPull = true;
                    continue;
                }
            }
            break;
        }
        return result;
    }

    // TODO Does pullImageCmd split the tag part?
    public static boolean pullImage(String imageName) {
        DockerClient client = DockerClientFactory.instance().client();

        boolean result = false;
        try {
            client.pullImageCmd(imageName)
                // .withTag("latest")
                .exec(new PullImageResultCallback())
                .awaitCompletion();
            result = true;
        } catch (Exception e) {
        }

        return result;
    }
}

package org.aksw.jenax.engine.docker.common;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Volume;

public class ContainerPathResolver {
    private static final Logger logger = LoggerFactory.getLogger(ContainerPathResolver.class);

    private final Map<Path, Path> containerToHostPaths = new LinkedHashMap<>();

    private ContainerPathResolver(Map<Path, Path> mountMap) {
        this.containerToHostPaths.putAll(mountMap);
    }

    /** Returns null if the argument container path does not resolve to a path on the host. */
    public Path resolveOrNull(Path containerPath) {
        Path result = null;
        for (Map.Entry<Path, Path> entry : containerToHostPaths.entrySet()) {
            Path prefix = entry.getKey();
            if (containerPath.startsWith(prefix)) {
                Path relative = prefix.relativize(containerPath);
                result = entry.getValue().resolve(relative);
                break;
            }
        }
        return result;
    }

    public static Path expectResolvePath(ContainerPathResolver containerPathResolver, Path path) {
        Path result = resolvePath(containerPathResolver, path, true);
        return result;
    }

    public static String resolvePathString(ContainerPathResolver containerPathResolver, String inPathStr) {
        String result = inPathStr;
        if (containerPathResolver != null) {
            Path inPath = Path.of(inPathStr);
            Path outPathOrNull = containerPathResolver.resolveOrNull(inPath);
            if (outPathOrNull != null) {
                result = outPathOrNull.toString();
            }
        }
        return result;
    }

    public static Path resolvePath(ContainerPathResolver containerPathResolver, Path path) {
        Path result = resolvePath(containerPathResolver, path, false);
        return result;
    }

    /** Never returns null. Returns the argument if no further resolution applied. */
    public static Path resolvePath(ContainerPathResolver containerPathResolver, Path path, boolean expectedToResolve) {
        Objects.requireNonNull(path);
        Path result = null;
        if (containerPathResolver != null) {
            result = containerPathResolver.resolveOrNull(path);
            if (result == null && expectedToResolve) {
                logger.warn("Container path unexpectedly not mapped to a host path: " + path);
                logger.warn("This will likely cause follow-up problems.");
            } else {
                logger.info("Container path -> Host path: " + path + " -> " + result);
            }
        }

        if (result == null) {
            result = path;
        }

        return result;
    }

    public static ContainerPathResolver create() {
        ContainerPathResolver result = null;
        try {
            // Note: Closing the global docker client raises an exception that is must not be closed.
            DockerClient dockerClient = DockerClientFactory.instance().client();
            InspectContainerResponse inspection = ContainerUtils.detectContainer(dockerClient);
            if (inspection != null) {
                result = create(inspection);
            }
        } catch (Exception e) {
            logger.info("Did not detect docker deamon.", e);
        }
        return result;
    }

    public static Map<Path, Path> getMountMap(InspectContainerResponse containerInfo) {
        Objects.requireNonNull(containerInfo);
        Map<Path, Path> mountMap = new LinkedHashMap<>();
        for (InspectContainerResponse.Mount mount : containerInfo.getMounts()) {
            String source = mount.getSource();
            Volume destination = mount.getDestination();

            // Heuristic: if source is an absolute path, it's likely a bind mount
            if (source != null && source.startsWith("/")) {
                String destPath = destination.getPath();
                Path containerPath = Paths.get(destPath);
                Path hostPath = Paths.get(source);
                mountMap.put(containerPath, hostPath);
            }
        }
        return mountMap;
    }

    public static ContainerPathResolver create(InspectContainerResponse containerInfo) {
        Map<Path, Path> mountMap = getMountMap(containerInfo);
        logger.info("Mount map: " + mountMap);
        return new ContainerPathResolver(mountMap);
    }
}

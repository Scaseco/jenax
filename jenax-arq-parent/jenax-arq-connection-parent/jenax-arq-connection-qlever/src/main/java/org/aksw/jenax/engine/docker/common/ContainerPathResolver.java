package org.aksw.jenax.engine.docker.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.NetworkSettings;
import com.github.dockerjava.api.model.Volume;

public class ContainerPathResolver {
    private static final Logger logger = LoggerFactory.getLogger(ContainerPathResolver.class);

    private final Map<Path, Path> containerToHostPaths = new HashMap<>();

    private ContainerPathResolver(Map<Path, Path> mountMap) {
        this.containerToHostPaths.putAll(mountMap);
    }

    public static Path resolvePath(ContainerPathResolver containerPathResolver, Path path) {
        Path result;
        if (containerPathResolver != null) {
            result = containerPathResolver.resolve(path);
            logger.info("Resolved container path: " + path + " -> " + result);
        } else {
            result = path;
        }
        return result;
    }

    public static ContainerPathResolver create() {
        DockerClient dockerClient = DockerClientFactory.instance().client();
        InspectContainerResponse containerInfo = detectContainerId(dockerClient);
        if (containerInfo == null) {
            return null;
        }
        logger.info("Detected container ID: " + containerInfo.getId());


        Map<Path, Path> mountMap = new HashMap<>();
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

        logger.info("Mount map: " + mountMap);
        return new ContainerPathResolver(mountMap);
    }

    public Path resolve(Path containerPath) {
        Path result = containerPath;
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

//    public static String detectContainerId() {
//        try {
//            String cpuset = java.nio.file.Files.readString(Paths.get("/proc/1/cpuset")).trim();
//            if (cpuset.isEmpty() || "/".equals(cpuset)) return null;
//            return cpuset.substring(cpuset.lastIndexOf("/") + 1);
//        } catch (IOException e) {
//            return null;
//        }
//    }
    private static InspectContainerResponse detectContainerId(DockerClient dockerClient) {
        Set<String> candidateIds;
        try {
            candidateIds = getContainerIdCandidates();
        } catch (IOException e) {
            logger.warn("Error trying to gather candidate container ids.", e);
            candidateIds = Set.of();
        }

        logger.info("Candidate container ids: " + candidateIds);
        InspectContainerResponse result = null;
        for (String candidateId : candidateIds) {
            try {
                result = dockerClient
                    .inspectContainerCmd(candidateId)
                    .exec();
            } catch (Exception e) {
                logger.info("Inspection failed for candidate containerId " + candidateId + ". Trying next.", e);
            }
        }
        return result;
    }

    private static Set<String> getContainerIdCandidates() throws IOException {
        Set<String> result = new LinkedHashSet<>();

        Path path;

        path = Paths.get("/proc/self/cgroup");
        if (Files.exists(path)) {
            // Try cgroup first (Docker, containerd, etc.)
            List<String> lines = java.nio.file.Files.readAllLines(Paths.get("/proc/self/cgroup"));
            for (String line : lines) {
                String[] parts = line.split("/");
                if (parts.length > 1) {
                    String candidate = parts[parts.length - 1];
                    if (isLikelyContainerId(candidate)) {
                        result.add(candidate);
                    }
                }
            }
        }

        path = Paths.get("/proc/1/cpuset");
        if (Files.exists(path)) {
            // Fallback: cpuset (e.g., /docker/<id> or /kubepods/<...>/<id>)
            String cpuset = java.nio.file.Files.readString(path).trim();
            String[] parts = cpuset.split("/");
            if (parts.length > 0) {
                String candidate = parts[parts.length - 1];
                if (isLikelyContainerId(candidate)) {
                    result.add(candidate);
                }
            }
        }

        path = Paths.get("/etc/hostname");
        if (Files.exists(path)) {
            String candidate = Files.readString(path).trim();
            if (candidate != null) {
                result.add(candidate);
            }
        }

        return result;
    }

    /**
     * Find and inspect the container that matches this process's hostname.
     *
     * @return InspectContainerResponse of the matching container
     * @throws IOException if /etc/hostname can't be read
     * @throws RuntimeException if the container cannot be found
     */
    public static InspectContainerResponse findSelfByHostname() throws IOException {
        String myHostname = Files.readString(Path.of("/etc/hostname")).trim();
        DockerClient docker = DockerClientFactory.instance().client();

        List<Container> containers = docker.listContainersCmd().withShowAll(true).exec();

        for (Container container : containers) {
            InspectContainerResponse inspect = docker.inspectContainerCmd(container.getId()).exec();
            String containerHostname = inspect.getConfig().getHostName();
            if (myHostname.equals(containerHostname)) {
                return inspect;
            }
        }

        throw new RuntimeException("Could not find container with hostname: " + myHostname);
    }

    private static boolean isLikelyContainerId(String s) {
        // Docker/containerd IDs are 64-char or 12-char lowercase hex
        boolean result = s.matches("[a-f0-9]{12,64}");
        logger.info("Is container id: " + s + " -> " + result);
        return result;
    }

//    public static GenericContainer<?> launchInSameNetworks(
//            DockerClient docker,
//            InspectContainerResponse primary,
//            String image
//        ) {
//            Set<String> networks = primary.getNetworkSettings().getNetworks().keySet();
//            Iterator<String> it = networks.iterator();
//            String primaryNetwork = it.next();
//
//            Set<String> remainingNetworks = new HashSet<>(networks);
//            remainingNetworks.remove(primaryNetwork);
//
//            GenericContainer<?> container = new GenericContainer<>(image)
//                .withNetworkMode(primaryNetwork);
//
//            container.start();
//
//            String id = container.getContainerId();
//            for (String network : remainingNetworks) {
//                docker.connectToNetworkCmd()
//                      .withContainerId(id)
//                      .withNetworkId(network)
//                      .exec();
//            }
//
//            return container;
//        }
}

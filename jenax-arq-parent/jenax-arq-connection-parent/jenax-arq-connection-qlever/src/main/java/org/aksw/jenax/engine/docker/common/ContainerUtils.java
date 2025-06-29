package org.aksw.jenax.engine.docker.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.aksw.jenax.engine.qlever.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;

public class ContainerUtils {
    private static final Logger logger = LoggerFactory.getLogger(ContainerUtils.class);

    private static Set<String> getContainerIdCandidates() {
        Set<String> result = new LinkedHashSet<>();

        Path path;

        path = Paths.get("/proc/self/cgroup");
        if (Files.exists(path)) {
            try {
                // Try cgroup first (Docker, containerd, etc.)
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    String[] parts = line.split("/");
                    if (parts.length > 1) {
                        String candidate = parts[parts.length - 1];
                        if (isLikelyContainerId(candidate)) {
                            result.add(candidate);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Error accessing " + path, e);
            }
        }

        path = Paths.get("/proc/1/cpuset");
        if (Files.exists(path)) {
            try {
                // Fallback: cpuset (e.g., /docker/<id> or /kubepods/<...>/<id>)
                String cpuset = Files.readString(path).trim();
                String[] parts = cpuset.split("/");
                if (parts.length > 0) {
                    String candidate = parts[parts.length - 1];
                    if (isLikelyContainerId(candidate)) {
                        result.add(candidate);
                    }
                }
            } catch (Exception e) {
                logger.warn("Error accessing " + path, e);
            }
        }

        String candidate = HostNameUtils.getHostName();
        if (candidate != null) {
            result.add(candidate);
        }

        return result;
    }

    /**
     * Find and inspect the container that matches this process's hostname.
     *
     * @return InspectContainerResponse of the matching container or null if none found.
     * @throws IOException if /etc/hostname can't be read
     */
//    public static InspectContainerResponse findSelfByHostname() throws IOException {
//        DockerClient docker = DockerClientFactory.instance().client();
//        return findSelfByHostname(docker);
//    }

    public static InspectContainerResponse findSelfByHostname(DockerClient docker) {
        List<Container> containers = docker.listContainersCmd().withShowAll(true).exec();
        String myHostName = HostNameUtils.getHostName();

        InspectContainerResponse result = null;
        for (Container container : containers) {
            InspectContainerResponse inspect = docker.inspectContainerCmd(container.getId()).exec();
            String containerHostName = inspect.getConfig().getHostName();
            if (myHostName.equals(containerHostName)) {
                result = inspect;
                break;
            }
        }

        return result;
    }

    private static boolean isLikelyContainerId(String s) {
        // Docker/containerd IDs are 64-char or 12-char lowercase hex
        boolean result = s.matches("[a-f0-9]{12,64}");
        logger.info("Is container id: " + s + " -> " + result);
        return result;
    }


//  public static String detectContainerId() {
//      try {
//          String cpuset = java.nio.file.Files.readString(Paths.get("/proc/1/cpuset")).trim();
//          if (cpuset.isEmpty() || "/".equals(cpuset)) return null;
//          return cpuset.substring(cpuset.lastIndexOf("/") + 1);
//      } catch (IOException e) {
//          return null;
//      }
//  }
    public static InspectContainerResponse detectContainer(DockerClient dockerClient) {
        Set<String> candidateIds = getContainerIdCandidates();
        logger.info("Candidate container ids: " + candidateIds);

        InspectContainerResponse result = null;
        for (String candidateId : candidateIds) {
            try {
                result = dockerClient
                    .inspectContainerCmd(candidateId)
                    .exec();

                if (result != null) {
                    break;
                }

            } catch (Exception e) {
                logger.info("Inspection failed for candidate containerId " + candidateId + ". Trying next.", e);
            }
        }

        if (result == null) {
            // Fallback to scanning all containers for a matching hostname.
            result = findSelfByHostname(dockerClient);
        }

        if (result != null) {
            logger.info("Detected container ID: " + result.getId());
        } else {
            logger.info("No container environment detected.");
        }

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

    public static <T extends GenericContainer<T>> GenericContainer<T> addCurrentUserAndGroup(GenericContainer<T> container) throws IOException {
        int uid = SystemUtils.getUID();
        int gid = SystemUtils.getGID();
        logger.info("Setting up container with UID: " + uid + ", GID: " + gid);
        @SuppressWarnings("resource")
        T result = container.withCreateContainerCmdModifier(cmd -> cmd.withUser(uid + ":" + gid));
        return result;
    }

    /** Check whether a command exists in an image and return its path. Null if not found. */
    public static String checkImageForCommand(String imageName, String commandName) {
        // which is a bash built in.
        String result = checkImage(imageName, "/usr/bin/bash", "which", commandName);

        if (result == null) {
            result = checkImage(imageName, "/usr/bin/sh", "/usr/bin/which", commandName);
        }

        return result;
    }

    @SuppressWarnings("resource")
    public static String checkImage(String imageName, String entryPoint, String whichCommand, String commandName) {
        try (GenericContainer<?> container = new GenericContainer<>(imageName)) {

            if (entryPoint != null) {
                container.withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint(entryPoint));
            }
            container.withCommand(whichCommand, commandName);
            // container.withCommand("/usr/bin/which", commandName);

            container.start();
            int exitCode = container.getCurrentContainerInfo().getState().getExitCodeLong().intValue();
            if (exitCode == 0) {
                List<String> logs = container.getLogs().lines().toList();
                return logs.isEmpty() ? null : logs.get(0).trim();
            }
        } catch (Exception e) {
            // Ignore and return null
        }
        return null;
    }
//
//    public static boolean canRunCommand(String imageName, String entrypoint, String command) {
//        try (GenericContainer<?> container = new GenericContainer<>(imageName)
//                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint(entrypoint))
//                .withCommand("-c", "exit 0")) {
//
//            container.start();
//            int exitCode = container.getCurrentContainerInfo().getState().getExitCodeLong().intValue();
//            return exitCode == 0;
//        } catch (Exception e) {
//            return false;
//        }
//    }


    public static boolean canRunEntrypoint(String imageName, String entrypoint, String commandPrefix) {
        int exitCode = runCommand(imageName, entrypoint, commandPrefix, "exit 0");
        return exitCode == 0;
    }

    public static boolean hasCommand(String imageName, String entrypoint, String commandPrefix, String command) {
        int exitCode = runCommand(imageName, entrypoint, commandPrefix, command);
        return exitCode != 127;
    }

    // "exit 0"
    public static int runCommand(String imageName, String entrypoint, String commandPrefix, String command) {
        try (GenericContainer<?> container = new GenericContainer<>(imageName)
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint(entrypoint))
                .withCommand(commandPrefix, command)) {

            container.start();
            int exitCode = container.getCurrentContainerInfo().getState().getExitCodeLong().intValue();
            return exitCode;
        } catch (Exception e) {
            return -1;
        }
    }
    /* Alternative probing strategies:
    // Use entry point sh with /usr/bin/which
    docker run --entrypoint '/usr/bin/sh' -i adfreiburg/qlever:latest "/usr/bin/which" lbzip2
    // Use bash where which is a shell built-in.
    docker run --entrypoint '/usr/bin/bash' -i adfreiburg/qlever:latest "which" lbzip2


    /usr/bin/lbzip2
    */
}

package jenax.engine.qlever.docker;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ContainerFetchException;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.startupcheck.StartupCheckStrategy;
import org.testcontainers.containers.traits.LinkableContainer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.core.CreateContainerCmdModifier;
import org.testcontainers.images.ImagePullPolicy;
import org.testcontainers.images.RemoteDockerImage;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.shaded.org.apache.commons.lang3.SystemUtils;
import org.testcontainers.shaded.org.checkerframework.checker.nullness.qual.NonNull;
import org.testcontainers.utility.MountableFile;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Volume;

public class GenericContainer<SELF extends GenericContainer<SELF>> implements Container<SELF> {

    public String[] buildCmdLine() {
        List<String> parts = new ArrayList<>();

        // Must not be closed - will raise: java.lang.IllegalStateException: You should never close the global DockerClient!
        DockerClient dockerClient = DockerClientFactory.lazyClient();

        String dockerImageName = getDockerImageName();

        CreateContainerCmd cmd = dockerClient.createContainerCmd(dockerImageName);

        applyConfiguration(cmd);

        parts.add("docker");

        parts.add("run");
        parts.add("-i");

        if (cmd.getWorkingDir() != null) {
            parts.add("-w");
            parts.add(workingDirectory);
        }

        String user = cmd.getUser();
        if (user != null) {
            parts.add("-u");
            parts.add(user);
        }

        for (Bind bind : cmd.getBinds()) {
            parts.add("-v");
            parts.add(bind.toString());
        }

        parts.add(getDockerImageName());

        parts.addAll(Arrays.asList(cmd.getCmd()));
        String[] result = parts.toArray(new String[0]);
        return result;
    }

    private ContainerDef containerDef = new ContainerDef();
    // private String containerName;
    private String workingDirectory;

    private final Set<CreateContainerCmdModifier> createContainerCmdModifiers = loadCreateContainerCmdCustomizers();


    private Long shmSize;
    private Map<String, String> tmpFsMapping;

    public GenericContainer(String imageName) {
        setDockerImageName(imageName);
    }

    private Set<CreateContainerCmdModifier> loadCreateContainerCmdCustomizers() {
        ServiceLoader<CreateContainerCmdModifier> containerCmdCustomizers = ServiceLoader
                .load(CreateContainerCmdModifier.class);
        Set<CreateContainerCmdModifier> loadedCustomizers = new LinkedHashSet<>();
        for (CreateContainerCmdModifier customizer : containerCmdCustomizers) {
            loadedCustomizers.add(customizer);
        }
        return loadedCustomizers;
    }


  /**
  * Set any custom settings for the create command such as shared memory size.
  */
 private HostConfig buildHostConfig(HostConfig config) {
     if (shmSize != null) {
         config.withShmSize(shmSize);
     }
     if (tmpFsMapping != null) {
         config.withTmpFs(tmpFsMapping);
     }
     return config;
 }


  private void applyConfiguration(CreateContainerCmd createCommand) {
      this.containerDef.applyTo(createCommand);
      buildHostConfig(createCommand.getHostConfig());

//      VolumesFrom[] volumesFromsArray = volumesFroms.stream().toArray(VolumesFrom[]::new);
//      createCommand.withVolumesFrom(volumesFromsArray);

//      Set<Link> allLinks = new HashSet<>();
//      Set<String> allLinkedContainerNetworks = new HashSet<>();
//      for (Entry<String, LinkableContainer> linkEntries : linkedContainers.entrySet()) {
//          String alias = linkEntries.getKey();
//          LinkableContainer linkableContainer = linkEntries.getValue();
//
//          Set<Link> links = findLinksFromThisContainer(alias, linkableContainer);
//          allLinks.addAll(links);
//
//          if (allLinks.size() == 0) {
//              throw new ContainerLaunchException(
//                  "Aborting attempt to link to container " +
//                  linkableContainer.getContainerName() +
//                  " as it is not running"
//              );
//          }
//
//          Set<String> linkedContainerNetworks = findAllNetworksForLinkedContainers(linkableContainer);
//          allLinkedContainerNetworks.addAll(linkedContainerNetworks);
//      }
//
//      createCommand.withLinks(allLinks.toArray(new Link[allLinks.size()]));
//
//      allLinkedContainerNetworks.remove("bridge");
//      if (allLinkedContainerNetworks.size() > 1) {
//          logger()
//              .warn(
//                  "Container needs to be on more than one custom network to link to other " +
//                  "containers - this is not currently supported. Required networks are: {}",
//                  allLinkedContainerNetworks
//              );
//      }
//
//      Optional<String> networkForLinks = allLinkedContainerNetworks.stream().findFirst();
//      if (networkForLinks.isPresent()) {
//          logger().debug("Associating container with network: {}", networkForLinks.get());
//          createCommand.withNetworkMode(networkForLinks.get());
//      }
//
//      if (hostAccessible) {
//          PortForwardingContainer.INSTANCE.start();
//      }
//      PortForwardingContainer.INSTANCE
//          .getNetwork()
//          .ifPresent(it -> {
//              withExtraHost(INTERNAL_HOST_HOSTNAME, it.getIpAddress());
//          });
//
//      String[] extraHostsArray = extraHosts.stream().toArray(String[]::new);
//      createCommand.withExtraHosts(extraHostsArray);

      if (workingDirectory != null) {
          createCommand.withWorkingDir(workingDirectory);
      }

      for (CreateContainerCmdModifier createContainerCmdModifier : this.createContainerCmdModifiers) {
          createCommand = createContainerCmdModifier.modify(createCommand);
      }
  }

    public SELF withCreateContainerCmdModifier(Consumer<CreateContainerCmd> modifier) {
        this.createContainerCmdModifiers.add(cmd -> {
            modifier.accept(cmd);
            return cmd;
        });
        return self();
    }

    @Override
    public String getContainerName() {
        throw new UnsupportedOperationException();
        // return containerName;
    }

    @Override
    public List<Integer> getExposedPorts() {
        return this.containerDef.getPortBindings().stream().map(PortBinding::getExposedPort).map(ExposedPort::getPort)
                .toList();
    }

    @Override
    public InspectContainerResponse getContainerInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCommand(@NonNull String command) {
        containerDef.setCommand(command.split(" "));
    }

    @Override
    public void setCommand(@NonNull String... commandParts) {
        containerDef.setCommand(commandParts);
    }

    @Override
    public void addEnv(String key, String value) {
        containerDef.addEnvVar(key, value);
    }

    @Override
    public void addLink(LinkableContainer otherContainer, String alias) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addExposedPort(Integer port) {
        containerDef.addExposedTcpPort(port);
    }

    @Override
    public void addExposedPorts(int... ports) {
        containerDef.addExposedTcpPorts(ports);

    }

    @Override
    public SELF waitingFor(@NonNull WaitStrategy waitStrategy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withFileSystemBind(String hostPath, String containerPath, BindMode mode) {
        addFileSystemBind(hostPath, containerPath, mode);
        return self();
    }

    @Override
    public void addFileSystemBind(final String hostPath, final String containerPath, final BindMode mode,
            final SelinuxContext selinuxContext) {
        if (SystemUtils.IS_OS_WINDOWS && hostPath.startsWith("/")) {
            // e.g. Docker socket mount
            this.containerDef.addBinds(
                    new Bind(hostPath, new Volume(containerPath), mode.accessMode, selinuxContext.selContext));
        } else {
            final MountableFile mountableFile = MountableFile.forHostPath(hostPath);
            this.containerDef.addBinds(new Bind(mountableFile.getResolvedPath(), new Volume(containerPath),
                    mode.accessMode, selinuxContext.selContext));
        }
    }

    @Override
    public SELF withVolumesFrom(Container container, BindMode mode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SELF withExposedPorts(Integer... ports) {
        setExposedPorts(List.of(ports));
        return self();
    }

    @Override
    public SELF withCopyFileToContainer(MountableFile mountableFile, String containerPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withCopyToContainer(Transferable transferable, String containerPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withEnv(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withEnv(Map<String, String> env) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withLabel(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withLabels(Map<String, String> labels) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withCommand(String cmd) {
        setCommand(cmd);
        return self();
    }

    @Override
    public SELF withCommand(String... commandParts) {
        setCommand(commandParts);
        return self();
    }

    @Override
    public SELF withExtraHost(String hostname, String ipAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withNetworkMode(String networkMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withNetwork(Network network) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withNetworkAliases(String... aliases) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withImagePullPolicy(ImagePullPolicy policy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withClasspathResourceMapping(String resourcePath, String containerPath, BindMode mode,
            SelinuxContext selinuxContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withStartupTimeout(Duration startupTimeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withPrivilegedMode(boolean mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withMinimumRunningDuration(Duration minimumRunningDuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withStartupCheckStrategy(StartupCheckStrategy strategy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withWorkingDirectory(String workDir) {
        this.workingDirectory = workDir;
        return self();
    }

    @Override
    public void setDockerImageName(@NonNull String dockerImageName) {
        containerDef.setImage(new RemoteDockerImage(dockerImageName));
    }

    @Override
    public String getDockerImageName() {
        Future<String> image = containerDef.getImage();
        try {
            return image.get();
        } catch (Exception e) {
            throw new ContainerFetchException("Can't get Docker image: " + image, e);
        }
    }

    @Override
    public String getTestHostIpAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SELF withLogConsumer(Consumer<OutputFrame> consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getPortBindings() {
        return this.containerDef.getPortBindings().stream()
                .map(it -> String.format("%s:%s", it.getBinding(), it.getExposedPort())).toList();
    }

    @Override
    public List<String> getExtraHosts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<String> getImage() {
        return containerDef.getImage();
    }

    @Override
    public List<String> getEnv() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getEnvMap() {
        return containerDef.getEnvVars();
    }

    @Override
    public String[] getCommandParts() {
        return containerDef.getCommand();
    }

    @Override
    public List<Bind> getBinds() {
        return containerDef.getBinds();
    }

    @Override
    public Map<String, LinkableContainer> getLinkedContainers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExposedPorts(List<Integer> exposedPorts) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPortBindings(List<String> portBindings) {
        this.containerDef.setPortBindings(portBindings.stream().map(PortBinding::parse).collect(Collectors.toSet()));
    }

    @Override
    public void setExtraHosts(List<String> extraHosts) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setImage(Future<String> image) {
        containerDef.setImage(new RemoteDockerImage(image));
    }

    @Override
    public void setEnv(List<String> env) {
        containerDef.setEnvVars(getEnvMap());
    }

    @Override
    public void setCommandParts(String[] commandParts) {
        containerDef.setCommand(commandParts);
    }

    @Override
    public void setBinds(List<Bind> binds) {
        containerDef.setBinds(binds);
    }

    @Override
    public void setLinkedContainers(Map<String, LinkableContainer> linkedContainers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWaitStrategy(WaitStrategy waitStrategy) {
        throw new UnsupportedOperationException();
    }
}

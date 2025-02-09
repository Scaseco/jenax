package jenax.engine.qlever.docker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineBuilder;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.engine.qlever.SystemUtils;
import org.aksw.jsheller.exec.CmdStrOps;
import org.aksw.jsheller.exec.SysRuntimeImpl;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;

public class RdfDataEngineBuilderQlever<X extends RdfDataEngineBuilderQlever<X>>
    implements RdfDataEngineBuilder<X>
{
    private static final Logger logger = LoggerFactory.getLogger(RdfDataEngineBuilderQlever.class);

    protected String hostDbDir;
    protected String qleverImageName;
    protected String qleverImageTag;
    protected Integer hostPort;
    protected QleverConfRun conf;

    public RdfDataEngineBuilderQlever() {
        this(null, null);
    }

    public RdfDataEngineBuilderQlever(String qleverImageName, String qleverImageTag) {
        super();
        this.qleverImageName = qleverImageName;
        this.qleverImageTag = qleverImageTag;
    }

    public static RdfDataEngine run(String hostDbDir, String qleverImageName, String qleverImageTag, Integer hostPort, QleverConfRun conf) throws NumberFormatException, IOException, InterruptedException {
        int uid = SystemUtils.getUID();
        int gid = SystemUtils.getGID();
        logger.info("Running as UID: " + uid + ", GID: " + gid);

        // Build command line
        List<String> cmdArgs = new ArrayList<>();
        QleverCliUtils.accumulateCliOptions(cmdArgs, conf);
        CmdStrOps strOps = SysRuntimeImpl.forCurrentOs().getStrOps();
        String cmdArgStr = cmdArgs.stream().map(strOps::quoteArg).collect(Collectors.joining(" "));
        String cmdStr = "ServerMain";
        if (!cmdArgStr.isEmpty()) {
            cmdStr += " " + cmdArgStr;
        }

        logger.info("Generated command line: " + cmdStr);

        if (qleverImageName == null) {
            qleverImageName = "adfreiburg/qlever";
        }

        if (qleverImageTag == null) {
            qleverImageTag = "commit-a307781";
        }

        String dockerImageName = Stream.of(qleverImageName, qleverImageTag)
            .filter(x -> x != null)
            .collect(Collectors.joining(":"));

        int defaultContainerPort = 8080;
        int containerPort = Optional.ofNullable(conf.getPort()).orElse(defaultContainerPort);

        // https://hub.docker.com/r/adfreiburg/qlever/tags
        org.testcontainers.containers.GenericContainer<?> container = new org.testcontainers.containers.GenericContainer<>(dockerImageName)
            .withWorkingDirectory("/data")
            .withExposedPorts(containerPort)
            // Setting UID does not work with latest image due to
            // error "UID 1000 already exists" ~ 2025-01-31
            // .withEnv("UID", Integer.toString(uid))
            // .withEnv("GID", Integer.toString(gid))
            .withCreateContainerCmdModifier(cmd -> cmd.withUser(uid + ":" + gid))
            .withFileSystemBind(hostDbDir, "/data", BindMode.READ_WRITE)
            .withCommand(new String[]{cmdStr})
            ;

        // Test containers will allocate a port if an explicit mapping is omitted.
        if (hostPort != null) {
            container.setPortBindings(List.of(hostPort + ":" + containerPort));
        }

        container.start();

        String serviceUrl = "http://" + container.getHost() + ":" + container.getMappedPort(containerPort);
        logger.info("Started Qlever server at: " + serviceUrl);

        container.followOutput(outputFrame -> {
            String msg = outputFrame.getUtf8StringWithoutLineEnding();
            logger.info(msg);
        });

        // TODO Make it possible to mutate the HTTP connection creation
        RdfDataEngine result = RdfDataEngines.of(
            () -> RDFConnectionRemote.service(serviceUrl).build(),
            () -> container.stop());

        return result;
    }

    @Override
    public RdfDataEngine build() throws Exception {
        RdfDataEngine result = run(hostDbDir, qleverImageName, qleverImageTag, hostPort, conf);

        // container.getDockerClient().waitContainerCmd(container.getContainerId()).exec(new WaitContainerResultCallback()).awaitCompletion();

        return result;
    }

    @Override
    public X setEngine(String engine) {
        throw new UnsupportedOperationException();
    }

    @Override
    public X setLocationContext(String locationContext) {
        throw new UnsupportedOperationException();
    }
    @Override
    public X setLocation(String location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public X setTempDir(String location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public X setLoader(String loader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public X setAutoDeleteIfCreated(Boolean onOrOff) {
        throw new UnsupportedOperationException();
    }

    @Override
    public X setProperty(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public X setProperties(Map<String, Object> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getEngine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocationContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTempDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLoader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean isAutoDeleteIfCreated() {
        throw new UnsupportedOperationException();
    }
}

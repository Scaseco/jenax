package jenax.engine.qlever.docker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jenax.engine.qlever.SystemUtils;
import org.aksw.jsheller.exec.CmdStrOps;
import org.aksw.jsheller.exec.SysRuntimeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;

import com.github.dockerjava.api.command.WaitContainerResultCallback;

public class QleverRunner {
    private static final Logger logger = LoggerFactory.getLogger(QleverRunner.class);

    public static void run(String hostDbDir, String qleverImageName, String qleverImageTag, Integer hostPort, QleverConfRun conf) throws NumberFormatException, IOException, InterruptedException {
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

        container.getDockerClient().waitContainerCmd(container.getContainerId()).exec(new WaitContainerResultCallback()).awaitCompletion();
    }
}

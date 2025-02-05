package jenax.engine.qlever;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import com.github.dockerjava.api.command.WaitContainerResultCallback;

public class TestQleverWrapper {
    private static final Logger logger = LoggerFactory.getLogger(TestQleverWrapper.class);

    // @Test
    public void test1() throws IOException, InterruptedException {
        int uid = SystemUtils.getUID();
        int gid = SystemUtils.getGID();
        System.out.println("Running as UID: " + uid + ", GID: " + gid);

        String hostDbDir = "/media/raven/T9/raven/qlever/wikidata/";
        String hostDbPrefix = "wikidata";
        String qleverDockerTag = "latest";
        String accessToken = "abcd";

        String dockerImageName = Stream.of("adfreiburg/qlever", qleverDockerTag)
            .filter(x -> x != null)
            .collect(Collectors.joining(":"));

        int containerPort = 8080;
        // https://hub.docker.com/r/adfreiburg/qlever/tags
        GenericContainer<?> container = new GenericContainer<>(dockerImageName)
            .withWorkingDirectory("/data")
            .withExposedPorts(containerPort)
            // Setting UID does not work with latest image due to
            // error "UID 1000 already exists" ~ 2025-01-31
            // .withEnv("UID", Integer.toString(uid))
            // .withEnv("GID", Integer.toString(gid))
            .withCreateContainerCmdModifier(cmd -> cmd.withUser(uid + ":" + gid))
            .withFileSystemBind(hostDbDir, "/data", BindMode.READ_WRITE)
            .withCommand(new String[]{"ServerMain -p " + containerPort + " -i " + hostDbPrefix})
            // .withCommand(new String[]{"ServerMain -h"})
            ;

            // Note: To force a host port use .setPortBindings(List.of("1111:2222"));

        container.start();

        String serviceUrl = "http://" + container.getHost() + ":" + container.getMappedPort(containerPort);
        System.out.println("Started at: " + serviceUrl);

        container.followOutput(outputFrame -> {
            String msg = outputFrame.getUtf8String();
            logger.info(msg);
        });

        container.getDockerClient().waitContainerCmd(container.getContainerId()).exec(new WaitContainerResultCallback()).awaitCompletion();

        // container.stop();

    }
}

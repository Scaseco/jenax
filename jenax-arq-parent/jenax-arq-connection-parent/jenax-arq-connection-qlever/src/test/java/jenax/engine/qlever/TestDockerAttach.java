package jenax.engine.qlever;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.shaded.com.github.dockerjava.core.command.AttachContainerResultCallback;
import org.testcontainers.utility.DockerImageName;

import com.github.dockerjava.api.command.AttachContainerCmd;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Frame;

public class TestDockerAttach {
    Logger log = LoggerFactory.getLogger("MyLogger");

    @Test
    @Ignore
    public void test() throws IOException, InterruptedException {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("ubuntu:24.04"))
            .withCommand("gzip", "-cd")
            .withCreateContainerCmdModifier(cmd -> {
                cmd.withTty(false);
                cmd.withStdinOpen(true);
                cmd.withUser("1000:1000");
                cmd.withAttachStdin(true).withAttachStdout(true).withAttachStderr(true);
            })
            .withLogConsumer(new Slf4jLogConsumer(log));

        container.start();

        Thread.sleep(2000);
        System.out.println("Container Started");

        // 1) Create gzipped data in memory
        String uncompressedText = "Hello\n";
        ByteArrayOutputStream gzippedBytes = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(gzippedBytes)) {
            gzipOut.write(uncompressedText.getBytes());
            // GZIPOutputStream closes on `close()` which finalizes the gzip trailer
        }

        AttachContainerCmd attachCmd;
        ByteArrayInputStream gzipStream = new ByteArrayInputStream(gzippedBytes.toByteArray());

        PipedOutputStream stdOut = new PipedOutputStream();
        PipedInputStream stdIn = new PipedInputStream(stdOut);

        // 2) Attach the gzipped data to stdin of the running container
        attachCmd = container.getDockerClient()
            .attachContainerCmd(container.getContainerId())
            .withLogs(true)
            .withStdIn(stdIn)
            .withStdOut(true)
            .withStdErr(true)
            .withTimestamps(false)
            // .withFollowStream(true)
            ;


        // 3) We'll capture the container’s stdout frames (the decompressed text)
        AttachContainerResultCallback callback = new AttachContainerResultCallback() {
            @Override
            public void onNext(Frame frame) {
                // frame.getPayload() is the decompressed data from gzip -cd
                String output = new String(frame.getPayload());
                System.out.print(output); // or log it or store it
                super.onNext(frame);
            }
        };

        // 4) Execute the attach command and wait
        System.out.println("Trying to attach");

        AttachContainerResultCallback tmp = attachCmd.exec(callback);
        tmp.awaitStarted();
        System.out.println("Started.");

        gzipStream.transferTo(stdOut);
        // stdOut.close();
        stdOut.flush();
        stdOut.close();
        // stdOut.close();
        // stdIn.close();
        // tmp.awaitCompletion();
//        try {
//             //.awaitCompletion();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }

        tmp.awaitCompletion();

        System.out.println("Done");

        container.getDockerClient()
        .waitContainerCmd(container.getContainerId())
        .exec(new WaitContainerResultCallback())
        .awaitCompletion();

    container.stop();
    }
}

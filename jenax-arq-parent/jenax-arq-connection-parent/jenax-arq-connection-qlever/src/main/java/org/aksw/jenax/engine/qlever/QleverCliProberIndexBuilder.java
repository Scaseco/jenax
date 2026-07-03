package org.aksw.jenax.engine.qlever;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.ProcessIoWrapper;
import org.aksw.shellgebra.exec.graph.ProcessIoWrapper.ExecResult;
import org.aksw.shellgebra.processbuilder.ProcessBuilderDockerRun;

public class QleverCliProberIndexBuilder {
    public enum CliType {
        IndexBuilderMain("IndexBuilderMain"),
        QleverIndex("qlever-index");

        // Could extend this enum into a lambda that builds the command.

        private String commandName;

        private CliType(String commandName) {
            this.commandName = commandName;
        }

        public String getCommandName() {
            return commandName;
        }
    }

    /** Probe for the index builder cli */
    public static Optional<CliType> probe(String imageName) {
        Objects.requireNonNull(imageName);
        Optional<CliType> result;
        if (isVersion2(imageName)) {
            result = Optional.of(CliType.QleverIndex);
        } else if (isVersion1(imageName)) {
            result = Optional.of(CliType.IndexBuilderMain);
        } else {
            result = Optional.empty();
        }
        return result;
    }

    private static boolean isVersion2(String imageName) {
        try {
            ExecResult er = exec(imageName, "qlever-index -h");
            if (er.out().contains("--kg-input-file")) {
                return true;
            }
        } catch (Exception e) {
            // TODO If it is command-not-found then ignore - otherwise log a warning.
        }
        return false;
    }

    private static boolean isVersion1(String imageName) {
        try {
            ExecResult er = exec(imageName, "IndexBuilderMain -h");
            if (er.out().contains("--kg-input-file")) {
                return true;
            }
        } catch (Exception e) {
            // TODO If it is command-not-found then ignore - otherwise log a warning.
        }
        return false;
    }


    static ExecResult exec(String imageName, String... command) throws IOException, InterruptedException {
        // docker run -it --rm -u $(id -u):$(id -g) -v $(pwd):/data -w /data adfreiburg/qlever:latest "qlever index --help"
        Path path = Files.createTempDirectory("qlever-file-mapper");
        try {
            FileMapper fileMapper = FileMapper.of(path.toString());
            Process p = ProcessBuilderDockerRun.of(command)
                .imageRef(imageName)
                .fileMapper(fileMapper)
                .interactive(false)
                .start();

            ExecResult er = ProcessIoWrapper.builder(p).consume();
            // System.out.println(er.out());
            // System.err.println(er.err());
            return er;
        } finally {
            Files.deleteIfExists(path);
        }
    }
}

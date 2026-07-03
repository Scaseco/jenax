package org.aksw.jenax.engine.qlever;

import java.util.Objects;
import java.util.Optional;

import org.aksw.shellgebra.exec.graph.ProcessIoWrapper.ExecResult;

public class QleverCliProberServer {
    public enum CliType {
        ServerMain("ServerMain"),
        QleverServer("qlever-server");

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
            result = Optional.of(CliType.QleverServer);
        } else if (isVersion1(imageName)) {
            result = Optional.of(CliType.ServerMain);
        } else {
            result = Optional.empty();
        }
        return result;
    }

    private static boolean isVersion2(String imageName) {
        try {
            ExecResult er = QleverCliProberIndexBuilder.exec(imageName, "qlever-server -h");
            if (er.out().contains("--access-token")) {
                return true;
            }
        } catch (Exception e) {
            // TODO If it is command-not-found then ignore - otherwise log a warning.
        }
        return false;
    }

    private static boolean isVersion1(String imageName) {
        try {
            ExecResult er = QleverCliProberIndexBuilder.exec(imageName, "ServerMain -h");
            if (er.out().contains("--access-token")) {
                return true;
            }
        } catch (Exception e) {
            // TODO If it is command-not-found then ignore - otherwise log a warning.
        }
        return false;
    }
}

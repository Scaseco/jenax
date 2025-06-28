package org.aksw.jenax.engine.docker.common;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.aksw.jenax.engine.qlever.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that implements various strategies to get the current system's hostname.
 *
 * Source: https://stackoverflow.com/questions/7348711/recommended-way-to-get-hostname-in-java
 */
public class HostNameUtils {
    private static final Logger logger = LoggerFactory.getLogger(HostNameUtils.class);

    public static String getHostName() {
        String result = getHostNameByHostNameCommand();
        if (result == null) {
            result = getHostNameByEtcHostNameFile();
            if (result == null) {
                result = getHostNameByMxBean();
                if (result == null) {
                    result = getHostNameByInet();
                }
            }
        }
        return result;
    }

    public static String getHostNameByHostNameCommand() {
        return getOrNull(() -> Optional.ofNullable(SystemUtils.getCommandOutput("hostname"))
                .map(String::trim).orElse(null));
    }

    public static String getHostNameByEtcHostNameFile() {
        Path file = Path.of("/etc/hostname");
        String result = getOrNull(() -> Files.exists(file) ? Files.readString(file).trim() : null);
        return result;
    }

    public static String getHostNameByMxBean() {
        return ManagementFactory.getRuntimeMXBean().getName().replaceAll("^[^@]*@", "");
    }

    public static String getHostNameByInet() {
        String result;
        try {
            result = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.debug("Could not resolve localhost ", e);
            result = null;
        }
        return result;
    }

//    protected static String readUtf8StringOrNull(Path path) {
//        String result = null;
//        if (Files.exists(path)) {
//            try {
//                result = Files.readString(path, StandardCharsets.UTF_8).trim();
//            } catch (Exception e) {
//                /// XXX Warn specifically if no read permission.
//                logger.debug("Could not read " + path, e);
//            }
//        }
//        return result;
//    }

    protected static <T> T getOrNull(Callable<T> callable) {
        T result;
        try {
            result = callable.call();
        } catch (Exception e) {
            logger.info("Execution error", e);
            result = null;
        }
        return result;
    }
}

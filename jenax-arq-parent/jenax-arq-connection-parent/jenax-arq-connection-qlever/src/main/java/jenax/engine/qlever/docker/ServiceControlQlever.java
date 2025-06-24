package jenax.engine.qlever.docker;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

public class ServiceControlQlever
    extends ServiceControlOverGenericContainer
{
    private static final Logger logger = LoggerFactory.getLogger(ServiceControlQlever.class);

    protected QleverServerConfig config;

    public ServiceControlQlever(GenericContainer<?> container, QleverServerConfigPojo config) {
        super(container);
        this.config = Objects.requireNonNull(config);
    }

    QleverServerConfig getConfig() {
        return config;
    }

    @Override
    public void start() {
        super.start();
        String serviceUrl = getDestination();
        logger.info("Started Qlever server at: " + serviceUrl);
    }

    protected String getDestination() {
        if (!isRunning()) {
            throw new RuntimeException("Cannot infer destination URL because container is stopped.");
        }

        Integer containerPort = getConfig().getPort();
        if (containerPort == null) {
            throw new RuntimeException("Container port must be set.");
        }

        GenericContainer<?> container = getBackend();
        String serviceUrl = "http://" + container.getHost() + ":" + container.getMappedPort(containerPort);
        return serviceUrl;
    }
}

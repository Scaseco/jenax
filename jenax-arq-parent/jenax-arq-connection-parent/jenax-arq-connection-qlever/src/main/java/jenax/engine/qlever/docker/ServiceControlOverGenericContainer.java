package jenax.engine.qlever.docker;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.engine.ServiceControl;

public class ServiceControlOverGenericContainer
    implements ServiceControl
{
    protected org.testcontainers.containers.GenericContainer<?> container;

    public ServiceControlOverGenericContainer(org.testcontainers.containers.GenericContainer<?> container) {
        super();
        this.container = Objects.requireNonNull(container);
    }

    @Override
    public org.testcontainers.containers.GenericContainer<?> getBackend() {
        return container;
    }

    @Override
    public void start() {
        container.start();
    }

    @Override
    public void stop() {
        container.stop();
    }

    @Override
    public boolean isRunning() {
        return container.isRunning();
    }
}

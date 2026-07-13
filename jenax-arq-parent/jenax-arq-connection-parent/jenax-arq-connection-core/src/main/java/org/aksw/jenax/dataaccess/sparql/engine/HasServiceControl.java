package org.aksw.jenax.dataaccess.sparql.engine;

import java.util.Optional;

public interface HasServiceControl {
    /**
     * An engine may optionally expose a way to start and stop
     * the underlying service. Note that only a call to
     * {@link RDFEngine#close()} guarantees to stop the service and
     * free any resources. Only calling {@link ServiceControl#stop()} is generally
     * NOT sufficient.
     */
    Optional<ServiceControl> getServiceControl();
}

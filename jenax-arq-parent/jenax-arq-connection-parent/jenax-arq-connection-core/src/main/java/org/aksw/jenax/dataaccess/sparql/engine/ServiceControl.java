package org.aksw.jenax.dataaccess.sparql.engine;

public interface ServiceControl {
    void start();
    void stop();

    /** Whether the service is running and healthy. */
    boolean isRunning();


    /** Return the object that backs this facade. */
    default Object getBackend() {
        return null;
    }
}

package org.aksw.jenax.sparql.qudtlib;

import org.apache.jena.sys.JenaSubsystemLifecycle;

public class InitQudtlibFunctions implements JenaSubsystemLifecycle {
    @Override
    public void start() {
        Functions.register();
    }

    @Override
    public void stop() {

    }
}

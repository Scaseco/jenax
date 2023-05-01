package org.aksw.jenax.sparql.generate;

import org.apache.jena.sys.JenaSubsystemLifecycle;

public class InitGenerateFunctions implements JenaSubsystemLifecycle {
    @Override
    public void start() {
        Functions.register();
    }

    @Override
    public void stop() {

    }
}

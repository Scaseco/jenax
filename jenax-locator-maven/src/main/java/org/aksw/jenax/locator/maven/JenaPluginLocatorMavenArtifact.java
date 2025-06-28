package org.aksw.jenax.locator.maven;

import org.apache.jena.riot.system.streammgr.Locator;
import org.apache.jena.riot.system.streammgr.StreamManager;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginLocatorMavenArtifact
    implements JenaSubsystemLifecycle {

    public void start() {
        init();
    }

    @Override
    public void stop() { }

    public static void init() {
        StreamManager mgr = StreamManager.get();
        Locator locator = LocatorMavenArtifact.get();
        if (!mgr.locators().contains(locator)) {
            mgr.addLocator(locator);
        }
    }
}

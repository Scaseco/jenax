package org.aksw.jenax.model.osreo;

import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginOsreo
    implements JenaSubsystemLifecycle
{
    @Override
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        JenaPluginUtils.registerResourceClasses(
            HasProbeLocation.class,

            Shell.class,
            LocatorCommand.class
        );

        JenaPluginUtils.registerResourceClasses(
            ImageIntrospection.class,
            ShellSupport.class
        );
    }
}

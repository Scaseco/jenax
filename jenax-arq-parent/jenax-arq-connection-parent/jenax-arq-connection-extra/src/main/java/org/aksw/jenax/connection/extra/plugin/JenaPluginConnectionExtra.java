package org.aksw.jenax.connection.extra.plugin;

import org.aksw.jenax.connection.extra.RDFConnectionMetaData;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginConnectionExtra
    implements JenaSubsystemLifecycle
{
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        JenaPluginUtils.registerResourceClasses(RDFConnectionMetaData.class);
    }

    public static void init(Personality<RDFNode> p) {
    }
}

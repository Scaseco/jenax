package org.aksw.jena_sparql_api.core.plugin;

import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginJsaCore
    implements JenaSubsystemLifecycle
{
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        // JenaPluginUtils.registerResourceClasses(RDFConnectionMetaData.class);
    }

    public static void init(Personality<RDFNode> p) {
    }
}

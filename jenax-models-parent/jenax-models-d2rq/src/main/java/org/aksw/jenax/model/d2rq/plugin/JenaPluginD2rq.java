package org.aksw.jenax.model.d2rq.plugin;

import org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;


public class JenaPluginD2rq
    implements JenaSubsystemLifecycle {

    public void start() {
        JenaPluginD2rq.init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        JenaPluginUtils.registerResourceClasses(
                D2rqDatabase.class
        );
    }
}

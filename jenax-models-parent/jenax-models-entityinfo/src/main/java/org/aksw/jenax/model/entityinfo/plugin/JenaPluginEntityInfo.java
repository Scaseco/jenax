package org.aksw.jenax.model.entityinfo.plugin;

import org.aksw.dcat.ap.domain.api.Checksum;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfoDefault;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;


public class JenaPluginEntityInfo
    implements JenaSubsystemLifecycle {

    public void start() {
        JenaPluginEntityInfo.init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        JenaPluginUtils.registerResourceClasses(
                RdfEntityInfoDefault.class,
                Checksum.class);
    }
}

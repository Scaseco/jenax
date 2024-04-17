package org.aksw.jenax.model.foaf.plugin;

import org.aksw.jenax.model.foaf.domain.api.FoafAgent;
import org.aksw.jenax.model.foaf.domain.api.FoafOnlineAccount;
import org.aksw.jenax.model.foaf.domain.api.FoafPerson;
import org.aksw.jenax.model.foaf.domain.api.FoafThing;
import org.aksw.jenax.model.foaf.domain.api.HasFoafDepiction;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;


public class JenaPluginFoaf
    implements JenaSubsystemLifecycle {

    @Override
    public void start() {
        JenaPluginFoaf.init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        JenaPluginUtils.registerResourceClasses(
            FoafThing.class,
            FoafAgent.class,
            FoafPerson.class,
            FoafOnlineAccount.class
        );

        JenaPluginUtils.registerResourceClasses(
            HasFoafDepiction.class
        );
    }
}

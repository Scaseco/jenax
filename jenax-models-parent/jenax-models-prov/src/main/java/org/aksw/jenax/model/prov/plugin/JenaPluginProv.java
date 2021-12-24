package org.aksw.jenax.model.prov.plugin;

import org.aksw.jenax.model.prov.Activity;
import org.aksw.jenax.model.prov.Agent;
import org.aksw.jenax.model.prov.Entity;
import org.aksw.jenax.model.prov.Plan;
import org.aksw.jenax.model.prov.QualifiedAssociation;
import org.aksw.jenax.model.prov.QualifiedDerivation;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;


public class JenaPluginProv
    implements JenaSubsystemLifecycle {

    public void start() {
        JenaPluginProv.init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        JenaPluginUtils.registerResourceClasses(
                Plan.class,
                Activity.class,
                Agent.class,
                Entity.class,
                QualifiedDerivation.class,
                QualifiedAssociation.class);
    }
}

package org.aksw.jenax.model.prefix.plugin;

import org.aksw.jenax.model.prefix.domain.api.HasPrefixes;
import org.aksw.jenax.model.prefix.domain.api.PrefixDeclaration;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;


public class JenaPluginShacl
    implements JenaSubsystemLifecycle {

    @Override
    public void start() {
        JenaPluginShacl.init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        JenaPluginUtils.registerResourceClasses(
                PrefixDeclaration.class,
                HasPrefixes.class);
    }
}

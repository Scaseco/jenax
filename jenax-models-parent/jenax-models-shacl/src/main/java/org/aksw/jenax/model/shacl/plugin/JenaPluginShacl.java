package org.aksw.jenax.model.shacl.plugin;

import org.aksw.jenax.model.shacl.domain.HasPrefixes;
import org.aksw.jenax.model.shacl.domain.PrefixDeclaration;
import org.aksw.jenax.model.shacl.domain.ShPrefixMapping;
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
                ShPrefixMapping.class,
                HasPrefixes.class);
    }
}

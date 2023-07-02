package org.aksw.jenax.model.shacl.plugin;

import org.aksw.jenax.model.shacl.domain.ShHasPrefixes;
import org.aksw.jenax.model.shacl.domain.ShHasSparqlStatement;
import org.aksw.jenax.model.shacl.domain.ShHasTargets;
import org.aksw.jenax.model.shacl.domain.ShNodeShape;
import org.aksw.jenax.model.shacl.domain.ShPrefixDeclaration;
import org.aksw.jenax.model.shacl.domain.ShPrefixMapping;
import org.aksw.jenax.model.shacl.domain.ShPropertyShape;
import org.aksw.jenax.model.shacl.domain.ShShape;
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
                ShPrefixDeclaration.class,
                ShPrefixMapping.class,
                ShHasPrefixes.class,
                ShHasTargets.class,
                ShHasSparqlStatement.class,
                ShShape.class,
                ShNodeShape.class,
                ShPropertyShape.class);
    }
}

package org.aksw.jenax.model.polyfill.plugin;

import org.aksw.jenax.model.polyfill.domain.api.PolyfillConditionConjunction;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillConditionQuery;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillRewriteJava;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillSuggestionRule;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;


public class JenaPluginPolyfill
    implements JenaSubsystemLifecycle {

    @Override
    public void start() {
        JenaPluginPolyfill.init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        JenaPluginUtils.registerResourceClasses(
            PolyfillSuggestionRule.class,
            PolyfillConditionQuery.class,
            PolyfillConditionConjunction.class,
            PolyfillRewriteJava.class
        );

//        JenaPluginUtils.registerResourceClasses(
//            Profile.class,
//            Plugin.class,
//            PolyfillConfiguration.class,
//            PolyfillConfigurationSet.class
//        );
    }
}
